package backend.tinkoff.error

class TinkoffInternalError(
    override val message: String = "Unexpected response was received from Tinkoff"
) : Exception()
