package backend.tinkoff.error

class NotEnoughVirtualSecurityError(
    override val message: String = "There is not enough securities in the virtual account"
) : Exception()
