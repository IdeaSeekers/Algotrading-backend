package backend.tinkoff.error

data class TinkoffInternalError(
    override val message: String = "Unexpected response was received from Tinkoff"
) : Exception()
