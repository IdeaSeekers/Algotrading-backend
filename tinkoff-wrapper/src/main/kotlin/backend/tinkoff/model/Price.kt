package backend.tinkoff.model

sealed interface Price

object MarketPrice : Price

data class LimitedPrice(val quotation: Quotation) : Price
