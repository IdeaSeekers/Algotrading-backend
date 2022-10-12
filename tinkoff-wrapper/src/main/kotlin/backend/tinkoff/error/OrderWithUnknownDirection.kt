package backend.tinkoff.error

data class OrderWithUnknownDirection(
    override val message: String = "Cannot interact with the order with unknown OrderDirection"
) : Exception()
