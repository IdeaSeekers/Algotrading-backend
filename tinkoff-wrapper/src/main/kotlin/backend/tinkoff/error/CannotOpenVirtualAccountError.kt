package backend.tinkoff.error

class CannotOpenVirtualAccountError(
    override val message: String = "Cannot open a new virtual account"
) : Exception()
