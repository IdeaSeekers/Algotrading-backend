package backend.tinkoff.model

import backend.tinkoff.error.OrderWithUnknownDirection
import ru.tinkoff.piapi.contract.v1.OrderDirection as TinkoffOrderDirection

enum class OrderDirection {
    BUY,
    SELL,
}

fun orderDirectionFromTinkoff(tinkoffDirection: TinkoffOrderDirection): OrderDirection =
    when (tinkoffDirection) {
        ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_BUY -> OrderDirection.BUY
        ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_SELL -> OrderDirection.SELL
        else -> throw OrderWithUnknownDirection()
    }
