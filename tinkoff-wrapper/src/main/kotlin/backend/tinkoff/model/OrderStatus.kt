package backend.tinkoff.model

import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus

enum class OrderStatus {
    FILL,
    REJECTED,
    CANCELLED,
    NEW,
    PARTIALLY_FILL,
    UNKNOWN,
}

fun orderStatusFromTinkoff(tinkoffStatus: OrderExecutionReportStatus): OrderStatus =
    when (tinkoffStatus) {
        OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL -> OrderStatus.FILL
        OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_REJECTED -> OrderStatus.REJECTED
        OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_CANCELLED -> OrderStatus.CANCELLED
        OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_NEW -> OrderStatus.NEW
        OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_PARTIALLYFILL -> OrderStatus.PARTIALLY_FILL
        OrderExecutionReportStatus.UNRECOGNIZED -> OrderStatus.UNKNOWN
        OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_UNSPECIFIED -> OrderStatus.UNKNOWN
    }
