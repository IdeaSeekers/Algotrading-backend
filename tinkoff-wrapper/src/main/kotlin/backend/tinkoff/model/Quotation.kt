package backend.tinkoff.model

import ru.tinkoff.piapi.contract.v1.Quotation as TinkoffQuotation

data class Quotation(
    val units: UInt,
    val nano: UInt,
) {

    fun isEqualToZero(): Boolean =
        units == 0U && nano == 0U

    fun toTinkoff(): TinkoffQuotation =
        TinkoffQuotation.newBuilder()
            .setUnits(units.toLong())
            .setNano(nano.toInt())
            .build()

    companion object {

        fun zero(): Quotation =
            Quotation(units = 0U, nano = 0U)

        fun fromTinkoff(quotation: TinkoffQuotation): Quotation =
            Quotation(quotation.units.toUInt(), quotation.nano.toUInt())
    }
}
