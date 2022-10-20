package backend.tinkoff.error

class OrderWithUnknownDirection(
    override val message: String = "Cannot interact with the order with unknown OrderDirection"
) : Exception()
