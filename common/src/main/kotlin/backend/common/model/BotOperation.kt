package backend.common.model

import java.time.Instant

data class BotOperation(
    val type: Type,
    val timestamp: Instant,
    val executedPrice: Double, // total price in rubles
    val returnValue: Double, // in rubles
) {
    enum class Type {
        BUY,
        SELL,
    }
}
