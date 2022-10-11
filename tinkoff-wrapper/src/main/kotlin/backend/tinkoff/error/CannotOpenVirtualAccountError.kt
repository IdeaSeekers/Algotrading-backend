package backend.tinkoff.error

data class CannotOpenVirtualAccountError(
    override val message: String = "Cannot open a new virtual account"
) : Exception()
