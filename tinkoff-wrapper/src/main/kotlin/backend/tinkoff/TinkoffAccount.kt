package backend.tinkoff

import backend.tinkoff.model.*
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse

interface TinkoffAccount {

    fun postBuyOrder(figi: Figi, quantity: Long, price: Price): Result<PostOrderResponse>

    fun postSellOrder(figi: Figi, quantity: Long, price: Price): Result<PostOrderResponse>

    fun cancelOrder(orderId: OrderId): Result<CancelOrderResponse>

    fun replaceOrder(orderId: OrderId, quantity: Long, price: Price): Result<PostOrderResponse>

    fun getOrderStatus(orderId: OrderId): Result<OrderState>

    fun getOpenOrders(): Result<List<OrderState>>

    fun getPositions(): Result<PositionsResponse>
}
