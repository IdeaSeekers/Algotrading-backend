package backend.tinkoff.response

import backend.tinkoff.model.Figi
import backend.tinkoff.model.OrderId
import backend.tinkoff.model.OrderStatus
import backend.tinkoff.model.orderStatusFromTinkoff
import ru.tinkoff.piapi.contract.v1.OrderState as TinkoffOrderState

data class OrderState(
    val orderId: OrderId,
    val figi: Figi,
    val status: OrderStatus,
) {
    companion object {
        fun fromTinkoff(tinkoffOrderState: TinkoffOrderState): OrderState =
            OrderState(
                tinkoffOrderState.orderId,
                tinkoffOrderState.figi,
                orderStatusFromTinkoff(tinkoffOrderState.executionReportStatus),
            )
    }
}
