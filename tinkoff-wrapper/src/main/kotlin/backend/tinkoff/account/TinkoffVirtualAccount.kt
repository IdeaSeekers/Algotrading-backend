package backend.tinkoff.account

import backend.statistics.reporter.BotActionReporter
import backend.tinkoff.error.NoOpenOrderWithSuchIdError
import backend.tinkoff.error.NotEnoughVirtualMoneyError
import backend.tinkoff.error.NotEnoughVirtualSecurityError
import backend.tinkoff.error.waitForSuccess
import backend.tinkoff.model.*
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse
import backend.tinkoff.storage.CurrencyStorage
import backend.tinkoff.storage.SecurityStorage
import java.time.Instant

class TinkoffVirtualAccount(
    private val botUid: BotUid,
    private val actualAccount: TinkoffActualAccount,
    private val availableCurrencies: CurrencyStorage,
    private val availableSecurities: SecurityStorage,
) : TinkoffAccount {

    override fun postBuyOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> {
        validatePostOrder(figi, quantity, price).onFailure {
            return Result.failure(it)
        }
        return actualAccount.postBuyOrder(figi, quantity, price).onSuccess {
            onPostBuyOrder(it)
            onSuccessOrderIfExecuted(OrderState.fromPostOrderResponse(it))
        }
    }

    override fun postSellOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> {
        validateSellOrder(figi, quantity).onFailure {
            return Result.failure(it)
        }
        return actualAccount.postSellOrder(figi, quantity, price).onSuccess {
            onPostSellOrder(it)
            onSuccessOrderIfExecuted(OrderState.fromPostOrderResponse(it))
        }
    }

    override fun cancelOrder(orderId: OrderId): Result<CancelOrderResponse> {
        val orderToCancel = myOpenOrders[orderId]
            ?: return Result.failure(NoOpenOrderWithSuchIdError())
        return actualAccount.cancelOrder(orderId).onSuccess {
            onCancelOrder(orderToCancel)
        }
    }

    override fun replaceOrder(orderId: OrderId, quantity: UInt, price: LimitedPrice): Result<PostOrderResponse> {
        val orderToReplace = myOpenOrders[orderId]
            ?: return Result.failure(NoOpenOrderWithSuchIdError())
        validateReplaceOrder(orderToReplace, quantity, price).onFailure {
            return Result.failure(it)
        }
        return actualAccount.replaceOrder(orderId, quantity, price).onSuccess {
            onCancelOrder(orderToReplace)
            onPostOrder(it)
            onSuccessOrderIfExecuted(OrderState.fromPostOrderResponse(it))
        }
    }

    override fun getOrderStatus(orderId: OrderId): Result<OrderState> {
        return actualAccount.getOrderStatus(orderId).onSuccess {
            onSuccessOrderIfExecuted(it)
        }
    }

    override fun getOpenOrders(): Result<List<OrderState>> =
        actualAccount.getOpenOrders().map { orderStates ->
            syncStateWith(orderStates.map { it.orderId }.toSet())
            orderStates.filter { it.orderId in myOpenOrders }
        }

    fun getExecutedOrders(): Result<MutableMap<OrderId, OrderState>> =
        Result.success(myExecutedOrders)

    override fun getPositions(): Result<PositionsResponse> {
        getOpenOrders() // to sync
        val virtualPositionsResponse = PositionsResponse(
            availableCurrencies.getAll(),
            availableSecurities.getAll(),
        )
        return Result.success(virtualPositionsResponse)
    }

    override fun getLastPrice(figi: Figi): Result<Quotation> =
        actualAccount.getLastPrice(figi)

    override fun getLotByShare(figi: Figi): Result<Int> =
        actualAccount.getLotByShare(figi)

    fun getTotalBalance(): Result<Quotation> {
        val totalSecuritiesCost = availableSecurities.getAll().map { security ->
            val quantity = security.balance
            val lot = getLotByShare(security.figi)
                .onFailure { return Result.failure(it) }
                .getOrThrow()
            val price = getLastPrice(security.figi)
                .onFailure { return Result.failure(it) }
                .getOrThrow()
            price * quantity * lot.toUInt()
        }.reduce(Quotation::plus)
        val currentRubleBalance = availableCurrencies.get("rub")?.quotation ?: Quotation.zero()
        return Result.success(totalSecuritiesCost + currentRubleBalance)
    }

    // internal

    private val myOpenOrders: MutableMap<OrderId, OrderState> =
        mutableMapOf()

    private val myExecutedOrders: MutableMap<OrderId, OrderState> =
        mutableMapOf()

    private val botActionReporter = BotActionReporter()

    private fun syncStateWith(currentOpenOrdersIds: Set<OrderId>) {
        val executedOrderIds = myOpenOrders.keys.toSet() - currentOpenOrdersIds.toSet()
        executedOrderIds.forEach { executedOrderId ->
            val executedOrderState = myOpenOrders[executedOrderId]!!
            onSuccessOrder(executedOrderState)
        }
    }

    private fun computeTotalRequestedQuotation(figi: Figi, quantity: UInt, price: Price): Quotation {
        val requestedQuotation = when (price) {
            is LimitedPrice -> price.quotation
            is MarketPrice -> waitForSuccess { actualAccount.getLastPrice(figi) }
        }
        return requestedQuotation * quantity
    }

    private fun computeExtraRequestedQuotation(oldOrder: OrderState, quantity: UInt, price: Price): Quotation {
        val oldRequestedQuotation = oldOrder.totalCost.quotation
        val newRequestedQuotation = computeTotalRequestedQuotation(oldOrder.figi, quantity, price)
        return (newRequestedQuotation - oldRequestedQuotation) ?: Quotation.zero()
    }

    // validation

    private fun validatePostOrder(figi: Figi, quantity: UInt, price: Price): Result<Unit> {
        val requestedCurrency = Currency(
            isoCode = "rub", // TODO
            computeTotalRequestedQuotation(figi, quantity, price)
        )
        if (!availableCurrencies.hasEnough(requestedCurrency))
            return Result.failure(NotEnoughVirtualMoneyError())
        return Result.success(Unit)
    }

    private fun validateSellOrder(figi: Figi, quantity: UInt): Result<Unit> {
        if (!availableSecurities.hasEnough(Security(figi, quantity)))
            return Result.failure(NotEnoughVirtualSecurityError())
        return Result.success(Unit)
    }

    private fun validateReplaceOrder(orderToReplace: OrderState, quantity: UInt, price: Price): Result<Unit> =
        when (orderToReplace.direction) {
            OrderDirection.BUY -> validateReplaceBuyOrder(orderToReplace, quantity, price)
            OrderDirection.SELL -> validateReplaceSellOrder(orderToReplace, quantity)
        }

    private fun validateReplaceBuyOrder(orderToReplace: OrderState, quantity: UInt, price: Price): Result<Unit> {
        val extraRequestedCurrency = Currency(
            isoCode = "rub", // TODO
            computeExtraRequestedQuotation(orderToReplace, quantity, price)
        )
        if (!availableCurrencies.hasEnough(extraRequestedCurrency)) {
            return Result.failure(NotEnoughVirtualMoneyError())
        }
        return Result.success(Unit)
    }

    private fun validateReplaceSellOrder(orderToReplace: OrderState, quantity: UInt): Result<Unit> {
        if (quantity > orderToReplace.lotsRequested) {
            val extraRequestedQuantity = orderToReplace.lotsRequested - quantity
            if (!availableSecurities.hasEnough(Security(orderToReplace.figi, extraRequestedQuantity))) {
                return Result.failure(NotEnoughVirtualSecurityError())
            }
        }
        return Result.success(Unit)
    }

    // OrderState.BUY callbacks

    private fun onPostBuyOrder(postOrderResponse: PostOrderResponse) {
        myOpenOrders[postOrderResponse.orderId] = OrderState.fromPostOrderResponse(postOrderResponse)
        availableCurrencies.forceDecrease(postOrderResponse.totalCost)
    }

    private fun onSuccessBuyOrder(orderState: OrderState) {
        myOpenOrders.remove(orderState.orderId) ?: return
        val purchasedSecurity = Security(orderState.figi, orderState.lotsExecuted)
        availableSecurities.forceIncrease(purchasedSecurity)
        myExecutedOrders[orderState.orderId] = orderState

        botActionReporter.executedBuy(
            botUid,
            orderState.figi,
            orderState.lotsExecuted,
            orderState.totalCost.quotation.toDouble(),
            Instant.now()
        )
    }

    private fun onCancelBuyOrder(orderState: OrderState) {
        myOpenOrders.remove(orderState.orderId) ?: return
        availableCurrencies.forceIncrease(orderState.totalCost)
    }

    // OrderState.SELL callbacks

    private fun onPostSellOrder(postOrderResponse: PostOrderResponse) {
        myOpenOrders[postOrderResponse.orderId] = OrderState.fromPostOrderResponse(postOrderResponse)
        val requestedSecurity = Security(postOrderResponse.figi, postOrderResponse.lotsRequested)
        availableSecurities.forceDecrease(requestedSecurity)
    }

    private fun onSuccessSellOrder(orderState: OrderState) {
        myOpenOrders.remove(orderState.orderId) ?: return
        availableCurrencies.forceIncrease(orderState.totalCost)
        myExecutedOrders[orderState.orderId] = orderState

        botActionReporter.executedSell(
            botUid,
            orderState.figi,
            orderState.lotsExecuted,
            orderState.totalCost.quotation.toDouble(),
            Instant.now()
        )
    }

    private fun onCancelSellOrder(orderState: OrderState) {
        myOpenOrders.remove(orderState.orderId) ?: return
        val requestedSecurity = Security(orderState.figi, orderState.lotsRequested)
        availableSecurities.forceIncrease(requestedSecurity)
    }

    // Callbacks managers

    private fun onPostOrder(postOrderResponse: PostOrderResponse) {
        when (postOrderResponse.direction) {
            OrderDirection.BUY -> onPostBuyOrder(postOrderResponse)
            OrderDirection.SELL -> onPostSellOrder(postOrderResponse)
        }
    }

    private fun onSuccessOrder(orderState: OrderState) {
        when (orderState.direction) {
            OrderDirection.BUY -> onSuccessBuyOrder(orderState)
            OrderDirection.SELL -> onSuccessSellOrder(orderState)
        }
    }

    private fun onSuccessOrderIfExecuted(orderState: OrderState) {
        if (orderState.status == OrderStatus.FILL) {
            onSuccessOrder(orderState)
        }
    }

    private fun onCancelOrder(orderState: OrderState) {
        when (orderState.direction) {
            OrderDirection.BUY -> onCancelBuyOrder(orderState)
            OrderDirection.SELL -> onCancelSellOrder(orderState)
        }
    }
}