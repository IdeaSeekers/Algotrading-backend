package backend.tinkoff.error

data class NoOpenOrderWithSuchIdError(
    override val message: String = "There is no open Order with such OrderId"
) : Exception()
