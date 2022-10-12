package backend.tinkoff.account

import backend.tinkoff.error.*
import backend.tinkoff.error.waitForSuccess
import backend.tinkoff.model.*
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse
import backend.tinkoff.storage.CurrencyStorage
import backend.tinkoff.storage.SecurityStorage

class TinkoffVirtualAccount(
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
        }
    }

    override fun postSellOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> {
        validateSellOrder(figi, quantity).onFailure {
            return Result.failure(it)
        }
        return actualAccount.postBuyOrder(figi, quantity, price).onSuccess {
            onPostSellOrder(it)
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
            onPostOrder(orderToReplace)
        }
    }

    override fun getOrderStatus(orderId: OrderId): Result<OrderState> {
        if (!myOpenOrders.containsKey(orderId))
            return Result.failure(NoOpenOrderWithSuchIdError())
        return actualAccount.getOrderStatus(orderId).onSuccess(::onSuccessOrder)
    }

    override fun getOpenOrders(): Result<List<OrderState>> =
        actualAccount.getOpenOrders().map { orderStates ->
            orderStates.filterOnlyExecuted().forEach(::onSuccessOrder)
            orderStates.filter { it.orderId in myOpenOrders }
        }

    override fun getPositions(): Result<PositionsResponse> {
        val virtualPositionsResponse = PositionsResponse(
            availableCurrencies.getAll(),
            availableSecurities.getAll(),
        )
        return Result.success(virtualPositionsResponse)
    }

    // internal

    private val myOpenOrders: MutableMap<OrderId, PostOrderResponse> =
        mutableMapOf() // TODO: to OrderInfo

    private fun List<OrderState>.filterOnlyExecuted(): List<OrderState> =
        this.filter { it.status == OrderStatus.FILL }

    private fun computeTotalRequestedQuotation(figi: Figi, quantity: UInt, price: Price): Quotation {
        val requestedQuotation = when (price) {
            is LimitedPrice -> price.quotation
            is MarketPrice -> waitForSuccess { actualAccount.getLastPrice(figi) }
        }
        return requestedQuotation * quantity
    }

    private fun computeExtraRequestedQuotation(oldOrder: PostOrderResponse, quantity: UInt, price: Price): Quotation {
        val oldRequestedQuotation = oldOrder.totalCost.quotation
        val newRequestedQuotation = computeTotalRequestedQuotation(oldOrder.figi, quantity, price)
        return (oldRequestedQuotation - newRequestedQuotation) ?: Quotation.zero()
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

    private fun validateReplaceBuyOrder(orderToReplace: PostOrderResponse, quantity: UInt, price: Price): Result<Unit> {
        val extraRequestedCurrency = Currency(
            isoCode = "rub", // TODO
            computeExtraRequestedQuotation(orderToReplace, quantity, price)
        )
        if (!availableCurrencies.hasEnough(extraRequestedCurrency))
            return Result.failure(NotEnoughVirtualMoneyError())
        return Result.success(Unit)
    }

    private fun validateReplaceSellOrder(orderToReplace: PostOrderResponse, quantity: UInt): Result<Unit> {
        if (quantity > orderToReplace.lotsRequested) {
            val extraRequestedQuantity = orderToReplace.lotsRequested - quantity
            if (!availableSecurities.hasEnough(Security(orderToReplace.figi, extraRequestedQuantity)))
                return Result.failure(NotEnoughVirtualSecurityError())
        }
        return Result.success(Unit)
    }

    // OrderState.BUY callbacks

    private fun onPostBuyOrder(postOrderResponse: PostOrderResponse) {
        myOpenOrders[postOrderResponse.orderId] = postOrderResponse
        availableCurrencies.decrease(postOrderResponse.totalCost)
    }

    private fun onSuccessBuyOrder(orderState: OrderState) {
        myOpenOrders.remove(orderState.orderId)
        val purchasedSecurity = Security(orderState.figi, orderState.lotsExecuted)
        availableSecurities.increase(purchasedSecurity)
    }

    private fun onCancelBuyOrder(postOrderResponse: PostOrderResponse) {
        myOpenOrders.remove(postOrderResponse.orderId)
        availableCurrencies.decrease(postOrderResponse.totalCost)
    }

    // OrderState.SELL callbacks

    private fun onPostSellOrder(postOrderResponse: PostOrderResponse) {
        myOpenOrders[postOrderResponse.orderId] = postOrderResponse
        val requestedSecurity = Security(postOrderResponse.figi, postOrderResponse.lotsRequested)
        availableSecurities.decrease(requestedSecurity)
    }

    private fun onSuccessSellOrder(orderState: OrderState) {
        myOpenOrders.remove(orderState.orderId)
        availableCurrencies.increase(orderState.totalCost)
    }

    private fun onCancelSellOrder(postOrderResponse: PostOrderResponse) {
        myOpenOrders.remove(postOrderResponse.orderId)
        val requestedSecurity = Security(postOrderResponse.figi, postOrderResponse.lotsRequested)
        availableSecurities.increase(requestedSecurity)
    }

    // Direction managers

    private fun validateReplaceOrder(orderToReplace: PostOrderResponse, quantity: UInt, price: Price): Result<Unit> =
        when (orderToReplace.direction) {
            OrderDirection.BUY -> validateReplaceBuyOrder(orderToReplace, quantity, price)
            OrderDirection.SELL -> validateReplaceSellOrder(orderToReplace, quantity)
        }

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

    private fun onCancelOrder(postOrderResponse: PostOrderResponse) {
        when (postOrderResponse.direction) {
            OrderDirection.BUY -> onCancelBuyOrder(postOrderResponse)
            OrderDirection.SELL -> onCancelSellOrder(postOrderResponse)
        }
    }
}