package backend.tinkoff.response

import backend.tinkoff.model.*
import ru.tinkoff.piapi.contract.v1.PostOrderResponse as TinkoffPostOrderResponse

data class PostOrderResponse(
    val figi: Figi,
    val orderId: OrderId,
    val status: OrderStatus,
    val totalCost: Currency,
    val direction: OrderDirection,
    val lotsRequested: UInt,
    val lotsExecuted: UInt,
) {
    companion object {
        fun fromTinkoff(tinkoffResponse: TinkoffPostOrderResponse): PostOrderResponse =
            PostOrderResponse(
                tinkoffResponse.figi,
                tinkoffResponse.orderId,
                orderStatusFromTinkoff(tinkoffResponse.executionReportStatus),
                Currency.fromMoneyValue(tinkoffResponse.totalOrderAmount),
                orderDirectionFromTinkoff(tinkoffResponse.direction),
                tinkoffResponse.lotsRequested.toUInt(),
                tinkoffResponse.lotsExecuted.toUInt(),
            )
    }
}
