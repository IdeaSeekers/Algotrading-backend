package backend.tinkoff.error

data class NotEnoughVirtualSecurityError(
    override val message: String = "There is not enough securities in the virtual account"
) : Exception()
