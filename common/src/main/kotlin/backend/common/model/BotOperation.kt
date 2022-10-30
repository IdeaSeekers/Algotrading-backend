package backend.common.model

import java.time.LocalDateTime

data class BotOperation(
    val type: Type,
    val timestamp: LocalDateTime,
    val executedPrice: Double, // total price in rubles
    val returnValue: Double, // in rubles
) {
    enum class Type {
        BUY,
        SELL,
    }
}
