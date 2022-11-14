package backend.tinkoff.account

import backend.tinkoff.error.FigiNotFoundError
import backend.tinkoff.error.TinkoffInternalError
import backend.tinkoff.error.wrapTinkoffRequest
import backend.tinkoff.model.*
import backend.tinkoff.response.CancelOrderResponse
import backend.tinkoff.response.OrderState
import backend.tinkoff.response.PositionsResponse
import backend.tinkoff.response.PostOrderResponse
import ru.tinkoff.piapi.contract.v1.OrderDirection
import ru.tinkoff.piapi.contract.v1.PriceType
import ru.tinkoff.piapi.core.InvestApi
import java.util.*

class TinkoffActualAccount(
    token: UserToken,
    private val accountId: AccountId,
) : TinkoffAccount {

    override fun postBuyOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> =
        postOrder(figi, quantity, price, OrderDirection.ORDER_DIRECTION_BUY)

    override fun postSellOrder(figi: Figi, quantity: UInt, price: Price): Result<PostOrderResponse> =
        postOrder(figi, quantity, price, OrderDirection.ORDER_DIRECTION_SELL)

    override fun cancelOrder(orderId: OrderId): Result<CancelOrderResponse> {
        val cancelOrderResponseFuture = investApi.ordersService
            .cancelOrder(accountId, orderId)

        return wrapTinkoffRequest {
            val cancellationInstant = cancelOrderResponseFuture.get()
            CancelOrderResponse(cancellationInstant)
        }
    }

    override fun replaceOrder(orderId: OrderId, quantity: UInt, price: LimitedPrice): Result<PostOrderResponse> {
        val idempotencyKey = UUID.randomUUID().toString()
        val priceType = PriceType.PRICE_TYPE_UNSPECIFIED

        val replaceOrderResponseFuture = investApi.ordersService
            .replaceOrder(accountId, quantity.toLong(), price.quotation.toTinkoff(), idempotencyKey, orderId, priceType)

        return wrapTinkoffRequest {
            val replaceOrderResponse = replaceOrderResponseFuture.get()
            PostOrderResponse.fromTinkoff(replaceOrderResponse)
        }
    }

    override fun getOrderStatus(orderId: OrderId): Result<OrderState> {
        val orderStateFuture = investApi.ordersService
            .getOrderState(accountId, orderId)

        return wrapTinkoffRequest {
            val orderState = orderStateFuture.get()
            OrderState.fromTinkoff(orderState)
        }
    }

    override fun getOpenOrders(): Result<List<OrderState>> {
        val orderStatesFuture = investApi.ordersService
            .getOrders(accountId)

        return wrapTinkoffRequest {
            val orderStates = orderStatesFuture.get()
            orderStates.map(OrderState::fromTinkoff)
        }
    }

    override fun getPositions(): Result<PositionsResponse> {
        val positionsFuture = investApi.operationsService
            .getPositions(accountId)

        return wrapTinkoffRequest {
            val positions = positionsFuture.get()
            PositionsResponse.fromTinkoff(positions)
        }
    }

    override fun getLastPrice(figi: Figi): Result<Quotation> {
        val lastPrices = investApi.marketDataService.getLastPrices(listOf(figi)).get()
        if (lastPrices.size != 1) {
            return Result.failure(TinkoffInternalError())
        }
        val quotation = Quotation.fromTinkoff(lastPrices.first().price)
        if (quotation.isEqualToZero())
            return Result.failure(FigiNotFoundError())
        return Result.success(quotation)
    }

    override fun getLotByShare(figi: Figi): Result<Int> {
        val shareFuture = investApi.instrumentsService
            .getShareByFigi(figi)

        return wrapTinkoffRequest {
            shareFuture.get().lot
        }
    }

    // internal

    private val investApi = InvestApi.createSandbox(token)

    private fun postOrder(figi: Figi, quantity: UInt, price: Price, orderDirection: OrderDirection): Result<PostOrderResponse> {
        val (quotation, orderType) = price.splitForTinkoff()
        val orderId = UUID.randomUUID().toString()

        val postOrderResponseFuture = investApi.ordersService
            .postOrder(figi, quantity.toLong(), quotation, orderDirection, accountId, orderType, orderId)

        return wrapTinkoffRequest {
            val postOrderResponse = postOrderResponseFuture.get()
            PostOrderResponse.fromTinkoff(postOrderResponse)
        }
    }
}