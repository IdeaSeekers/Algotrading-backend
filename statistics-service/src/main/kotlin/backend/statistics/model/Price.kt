package backend.statistics.model

data class Price(
    val isoCode: String,
    val units: UInt,
    val nano: UInt,
) {

    operator fun plus(other: Price): Price {
        val allNano = nano + other.nano
        val newUnits = units + other.units + allNano / maxNano
        val newNano = allNano % maxNano
        return Price(isoCode, newUnits, newNano)
    }

    private val maxNano = 1e9.toUInt()
}
