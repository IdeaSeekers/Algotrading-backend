package backend.statistics.reporter

import backend.db.bots.BotsDatabase
import backend.db.bots.BotsDatabase.Companion.BUY_OPERATION_ID
import backend.db.bots.BotsDatabase.Companion.SELL_OPERATION_ID
import java.sql.Timestamp
import java.time.Instant

class BotActionReporter {

    private val botsDatabase = BotsDatabase()

    fun executedBuy(botId: Int, figi: String, quantity: UInt, price: Double, timestamp: Instant) {
        botsDatabase.addOperation(
            botId, BUY_OPERATION_ID, figi.toInt(), quantity.toInt(), price,
            Timestamp.from(timestamp)
        )
    }

    fun executedSell(botId: Int, figi: String, quantity: UInt, price: Double, timestamp: Instant) {
        botsDatabase.addOperation(
            botId, SELL_OPERATION_ID, figi.toInt(), quantity.toInt(), price,
            Timestamp.from(timestamp)
        )
    }
}
