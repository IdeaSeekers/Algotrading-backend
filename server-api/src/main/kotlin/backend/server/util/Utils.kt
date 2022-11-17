package backend.server.util

fun parseId(stringId: String?): Result<Int> {
    if (stringId == null)
        return Result.failure(Exception("Missing or malformed id"))
    val intId = stringId.toIntOrNull()
        ?: return Result.failure(Exception("id should be integer"))
    return Result.success(intId)
}
