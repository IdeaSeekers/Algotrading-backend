package backend.tinkoff.response

import backend.tinkoff.model.Currency
import backend.tinkoff.model.Security

data class PositionsResponse(
    val money: List<Currency>,
    val securities: List<Security>,
)
