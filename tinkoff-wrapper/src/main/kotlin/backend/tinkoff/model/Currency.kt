package backend.tinkoff.model

import ru.tinkoff.piapi.contract.v1.MoneyValue

data class Currency(
    val isoCode: IsoCode,
    val quotation: Quotation,
) {
    fun toMoneyValue(): MoneyValue =
        MoneyValue.newBuilder()
            .setCurrency(isoCode)
            .setUnits(quotation.units.toLong())
            .setNano(quotation.nano.toInt())
            .build()
}
