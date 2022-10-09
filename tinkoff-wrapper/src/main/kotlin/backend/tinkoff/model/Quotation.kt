package backend.tinkoff.model

data class Quotation(
    val units: Long,
    val nano: Int,
) {
    fun isEqualToZero(): Boolean =
        units == 0L && nano == 0
}
