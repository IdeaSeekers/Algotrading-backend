package backend.tinkoff.model

import ru.tinkoff.piapi.contract.v1.OrderType
import ru.tinkoff.piapi.contract.v1.Quotation as TinkoffQuotation

sealed interface Price {
    fun splitForTinkoff(): Pair<TinkoffQuotation, OrderType>
}

object MarketPrice : Price {
    override fun splitForTinkoff(): Pair<TinkoffQuotation, OrderType> =
        Pair(Quotation.zero().toTinkoff(), OrderType.ORDER_TYPE_MARKET)
}

data class LimitedPrice(val quotation: Quotation) : Price {
    override fun splitForTinkoff(): Pair<TinkoffQuotation, OrderType> =
        Pair(quotation.toTinkoff(), OrderType.ORDER_TYPE_LIMIT)
}
