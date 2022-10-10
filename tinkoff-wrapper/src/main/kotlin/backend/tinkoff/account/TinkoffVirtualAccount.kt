package backend.tinkoff.account

import backend.tinkoff.model.Figi
import backend.tinkoff.model.LimitedPrice
import backend.tinkoff.model.OrderId
import backend.tinkoff.model.Price
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.Positions
import backend.tinkoff.response.PostOrderResponse

class TinkoffVirtualAccount(
    private val actualAccount: TinkoffActualAccount,
) : TinkoffAccount {

    override fun postBuyOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> {
        TODO("Not yet implemented")
    }

    override fun postSellOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> {
        TODO("Not yet implemented")
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

    override fun getPositions(): Result<Positions> {
        TODO("Not yet implemented")
    }

    // internal

    private val myOrderIds: MutableSet<OrderId> = mutableSetOf()
}