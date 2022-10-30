package backend.common.model

import java.time.Instant

/**
 * Information about the profitability of a bot or strategy at [timestamp].
 */
data class ReturnInfo(
    val timestamp: Instant,
    val returnValue: Double, // in rubles
)
