package backend.common.model

import java.time.LocalDateTime

/**
 * Information about the profitability of a bot or strategy at [timestamp].
 */
data class ReturnInfo(
    val timestamp: LocalDateTime,
    val returnValue: Double, // in rubles
)
