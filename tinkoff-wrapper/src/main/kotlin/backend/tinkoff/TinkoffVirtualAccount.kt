package backend.tinkoff

import backend.tinkoff.model.*
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse

class TinkoffVirtualAccount(
    private val actualAccount: TinkoffActualAccount,
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

    // internal

    private val myOrderIds: MutableSet<OrderId> = mutableSetOf()
}