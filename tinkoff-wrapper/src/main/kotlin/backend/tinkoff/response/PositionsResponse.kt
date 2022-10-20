package backend.tinkoff.response

import backend.tinkoff.model.Currency
import backend.tinkoff.model.Quotation
import backend.tinkoff.model.Security
import ru.tinkoff.piapi.core.models.Positions as TinkoffPositions
import java.math.BigDecimal

data class PositionsResponse(
    val currencies: List<Currency>,
    val securities: List<Security>,
) {
    companion object {
        fun fromTinkoff(tinkoffPositions: TinkoffPositions): PositionsResponse {
            val money = tinkoffPositions.money.map {
                val (bigUnits, bigNano) = it.value.divideAndRemainder(BigDecimal.ONE)
                val units = bigUnits.toLong().toUInt()
                val nano = bigNano.multiply(BigDecimal(1e9.toInt().toString())).toLong().toUInt()
                Currency(isoCode = it.currency, Quotation(units, nano))
            }
            val securities = tinkoffPositions.securities.map {
                Security(it.figi, it.balance.toUInt())
            }
            return PositionsResponse(money, securities)
        }
    }
}
