package backend.tinkoff.response

import backend.tinkoff.model.*
import ru.tinkoff.piapi.contract.v1.OrderState as TinkoffOrderState

data class OrderState(
    val orderId: OrderId,
    val figi: Figi,
    val status: OrderStatus,
    val totalCost: Currency,
    val direction: OrderDirection,
    val lotsRequested: UInt,
    val lotsExecuted: UInt,
) {
    companion object {
        fun fromTinkoff(tinkoffOrderState: TinkoffOrderState): OrderState =
            OrderState(
                tinkoffOrderState.orderId,
                tinkoffOrderState.figi,
                orderStatusFromTinkoff(tinkoffOrderState.executionReportStatus),
                Currency.fromMoneyValue(tinkoffOrderState.totalOrderAmount),
                orderDirectionFromTinkoff(tinkoffOrderState.direction),
                tinkoffOrderState.lotsRequested.toUInt(),
                tinkoffOrderState.lotsExecuted.toUInt(),
            )
    }
}
