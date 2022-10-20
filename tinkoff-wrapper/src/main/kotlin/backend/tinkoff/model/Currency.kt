package backend.tinkoff.model

import ru.tinkoff.piapi.contract.v1.MoneyValue

data class Currency(
    val isoCode: IsoCode,
    val quotation: Quotation,
) : Position {

    fun toMoneyValue(): MoneyValue =
        MoneyValue.newBuilder()
            .setCurrency(isoCode)
            .setUnits(quotation.units.toLong())
            .setNano(quotation.nano.toInt())
            .build()

    operator fun plus(other: Currency): Currency? {
        if (isoCode != other.isoCode)
            return null
        val newQuotation = quotation + other.quotation
        return Currency(isoCode, newQuotation)
    }

    operator fun minus(other: Currency): Currency? {
        if (isoCode != other.isoCode)
            return null
        val newQuotation = (quotation - other.quotation)
            ?: return null
        return Currency(isoCode, newQuotation)
    }

    companion object {
        fun fromMoneyValue(moneyValue: MoneyValue): Currency =
            Currency(
                moneyValue.currency,
                Quotation(
                    moneyValue.units.toUInt(),
                    moneyValue.nano.toUInt()
                )
            )
    }
}
