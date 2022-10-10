package backend.tinkoff.model

data class Quotation(
    val units: Long,
    val nano: Int,
) {

    fun isEqualToZero(): Boolean =
        units == 0L && nano == 0

    companion object {
        fun from(quotation: ru.tinkoff.piapi.contract.v1.Quotation): Quotation =
            Quotation(quotation.units, quotation.nano)
    }
}
