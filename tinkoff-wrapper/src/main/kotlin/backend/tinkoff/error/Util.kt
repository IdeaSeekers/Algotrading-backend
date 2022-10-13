package backend.tinkoff.error

internal fun <T> wrapTinkoffRequest(requestToTinkoffApi: () -> T): Result<T> =
    try {
        Result.success(requestToTinkoffApi())
    } catch (e: Throwable) {
        Result.failure(e)
    }

internal fun <T> waitForSuccess(block: () -> Result<T>): T {
    while (true) {
        val result = block()
        if (result.isSuccess) {
            return result.getOrThrow()
        }
    }
}
