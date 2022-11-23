package backend.server.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

fun parseId(stringId: String?): Result<Int> {
    if (stringId == null)
        return Result.failure(Exception("Missing or malformed id"))
    val intId = stringId.toIntOrNull()
        ?: return Result.failure(Exception("id should be integer"))
    return Result.success(intId)
}

fun parseTimestamp(timestamp: String?): Instant? {
    if (timestamp == null)
        return null
    val dateTIme = try {
        LocalDateTime.parse(timestamp)
    } catch (e: Exception) {
        null
    }
    return dateTIme?.toInstant(ZoneOffset.UTC)
}
