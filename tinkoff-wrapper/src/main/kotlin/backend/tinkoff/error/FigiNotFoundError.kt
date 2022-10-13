package backend.tinkoff.error

class FigiNotFoundError(
    override val message: String = "Security with such FIGI does not exist"
) : Exception()
