package backend.tinkoff

import backend.tinkoff.error.FigiNotFoundError
import backend.tinkoff.error.TinkoffInternalError
import backend.tinkoff.model.*
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse
import ru.tinkoff.piapi.core.InvestApi

class TinkoffActualAccount(
    private val token: UserToken,
    private val accountId: AccountId,
) : TinkoffAccount {

    override fun postBuyOrder(figi: Figi, quantity: Long, price: Price): Result<PostOrderResponse> {
        TODO("Not yet implemented")
    }

    override fun postSellOrder(figi: Figi, quantity: Long, price: Price): Result<PostOrderResponse> {
        TODO("Not yet implemented")
    }

    override fun cancelOrder(orderId: OrderId): Result<CancelOrderResponse> {
        TODO("Not yet implemented")
    }

    override fun replaceOrder(orderId: OrderId, quantity: Long, price: Price): Result<PostOrderResponse> {
        TODO("Not yet implemented")
    }

    override fun getOrderStatus(orderId: OrderId): Result<OrderState> {
        TODO("Not yet implemented")
    }

    override fun getOpenOrders(): Result<List<OrderState>> {
        TODO("Not yet implemented")
    }

    override fun getPositions(): Result<PositionsResponse> {
        TODO("Not yet implemented")
    }

    fun getLastPrice(figi: Figi): Result<Quotation> {
        val lastPrices = investApi.marketDataService.getLastPrices(listOf(figi)).get()
        if (lastPrices.size != 1) {
            return Result.failure(TinkoffInternalError())
        }
        val tinkoffQuotation = lastPrices.first().price
        val quotation = Quotation(tinkoffQuotation.units, tinkoffQuotation.nano)
        if (quotation.isEqualToZero()) {
            return Result.failure(FigiNotFoundError())
        }
        return Result.success(quotation)
    }

    // internal

    private val investApi = InvestApi.create(token)
}