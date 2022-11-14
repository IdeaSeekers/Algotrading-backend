package backend.tinkoff.account

import backend.tinkoff.model.Figi
import backend.tinkoff.model.LimitedPrice
import backend.tinkoff.model.OrderId
import backend.tinkoff.model.Price
import backend.tinkoff.model.Quotation
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse

interface TinkoffAccount {

    fun postBuyOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse>

    fun postSellOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse>

    fun cancelOrder(orderId: OrderId): Result<CancelOrderResponse>

    fun replaceOrder(orderId: OrderId, quantity: UInt, price: LimitedPrice): Result<PostOrderResponse>

    fun getOrderStatus(orderId: OrderId): Result<OrderState>

    fun getOpenOrders(): Result<List<OrderState>>

    fun getPositions(): Result<PositionsResponse>

    fun getLastPrice(figi: Figi): Result<Quotation>

    fun getLotByShare(figi: Figi): Result<Int>
}
