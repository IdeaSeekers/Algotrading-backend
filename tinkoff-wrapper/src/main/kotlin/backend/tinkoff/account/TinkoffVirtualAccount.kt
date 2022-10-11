package backend.tinkoff.account

import backend.tinkoff.error.NotEnoughVirtualMoneyError
import backend.tinkoff.error.NotEnoughVirtualSecurityError
import backend.tinkoff.error.waitForSuccess
import backend.tinkoff.model.*
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse

class TinkoffVirtualAccount(
    private val actualAccount: TinkoffActualAccount,
    private val initialAvailablePositions: List<Position>,
) : TinkoffAccount {

    override fun postBuyOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> {
        if (!isEnoughMoney(figi, quantity, price))
            return Result.failure(NotEnoughVirtualMoneyError())

        return actualAccount.postBuyOrder(figi, quantity, price).onSuccess {
            myOpenOrders += it.orderId
            it.totalCost
        }
    }

    override fun postSellOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> {
        if (!hasEnoughFigi(figi, quantity))
            return Result.failure(NotEnoughVirtualSecurityError())

        return actualAccount.postBuyOrder(figi, quantity, price).onSuccess {
            myOpenOrders += it.orderId
        }
    }

    override fun cancelOrder(orderId: OrderId): Result<CancelOrderResponse> {
        TODO("Not yet implemented")
    }

    override fun replaceOrder(orderId: OrderId, quantity: UInt, price: LimitedPrice): Result<PostOrderResponse> {
        TODO("Not yet implemented")
    }

    override fun getOrderStatus(orderId: OrderId): Result<OrderState> {
        TODO("Not yet implemented")
    }

    override fun getOpenOrders(): Result<List<OrderState>> {
        TODO("Not yet implemented")
    }

    override fun getPositions(): Result<PositionsResponse> {
        actualAccount.getPositions().map { positionsResponse ->
            positionsResponse.currencies.filter { currency -> currency. }
        }
    }

    fun syncOrders() {

    }

    // internal

    private val myOpenOrders: MutableSet<OrderId> = mutableSetOf()

    private val availableCurrencies: MutableMap<IsoCode, Currency> = TODO()

    private val availableSecurities: MutableMap<Figi, Security> = TODO()

    private fun isEnoughMoney(figi: Figi, quantity: UInt, price: Price): Boolean {
        val availableMoney = availableCurrencies["rub"]?.quotation ?: Quotation.zero()
        val requestedPrice = when (price) {
            is LimitedPrice ->
                price.quotation
            is MarketPrice ->
                waitForSuccess { actualAccount.getLastPrice(figi) }
        }
        val requestedMoney = requestedPrice * quantity
        return requestedMoney <= availableMoney
    }

    private fun hasEnoughFigi(figi: Figi, quantity: UInt): Boolean {
        val availableSecurities = availableSecurities[figi]?.balance ?: 0U
        return quantity <= availableSecurities
    }

    private fun decreaseAvailableCurrency(currency: Currency) {
        availableCurrencies.merge(currency.isoCode, currency) { oldValue, decreaseBy ->
            (oldValue - decreaseBy)!!
        }
    }
}