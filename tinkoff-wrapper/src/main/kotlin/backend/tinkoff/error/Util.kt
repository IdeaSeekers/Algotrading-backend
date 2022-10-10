package backend.tinkoff.error

internal fun <T> wrapTinkoffRequest(requestToTinkoffApi: () -> T): Result<T> =
    try {
        Result.success(requestToTinkoffApi())
    } catch (e: Throwable) {
        Result.failure(e)
    }
