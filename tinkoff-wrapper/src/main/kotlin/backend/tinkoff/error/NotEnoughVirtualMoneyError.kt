package backend.tinkoff.error

class NotEnoughVirtualMoneyError(
    override val message: String = "There is not enough money in the virtual account"
) : Exception()
