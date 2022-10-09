package backend.tinkoff.error

data class FigiNotFoundError(
    override val message: String = "Security with such FIGI does not exist"
) : Exception()
