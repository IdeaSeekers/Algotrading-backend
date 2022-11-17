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

    fun toDouble(): Double =
        units.toDouble() + nano.toDouble() * 1e-9

    operator fun plus(other: Quotation): Quotation {
        val allNano = nano + other.nano
        val newUnits = units + other.units + allNano / maxNano
        val newNano = allNano % maxNano
        return Quotation(newUnits, newNano)
    }

    operator fun minus(other: Quotation): Quotation? {
        if (this < other)
            return null
        return if (nano < other.nano)
            Quotation(units - other.units - 1U, maxNano + nano - other.nano)
        else
            Quotation(units - other.units, nano - other.nano)
    }

    operator fun times(times: UInt): Quotation {
        val allNano: Long = nano.toLong() * times.toLong()
        val newUnits = (units.toLong() * times.toLong() + allNano / maxNano.toLong()).toUInt()
        val newNano = (allNano % maxNano.toLong()).toUInt()
        return Quotation(newUnits, newNano)
    }

    operator fun compareTo(other: Quotation): Int =
        when {
            units < other.units -> -1
            units > other.units -> +1
            nano < other.nano -> -1
            nano > other.nano -> +1
            else -> 0
        }

    companion object {

        fun zero(): Quotation =
            Quotation(units = 0U, nano = 0U)

        fun fromTinkoff(quotation: TinkoffQuotation): Quotation =
            Quotation(quotation.units.toUInt(), quotation.nano.toUInt())
    }

    // internal

    private val maxNano = 1e9.toUInt()
}
