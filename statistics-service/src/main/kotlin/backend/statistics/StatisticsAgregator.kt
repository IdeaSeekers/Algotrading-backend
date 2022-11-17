package backend.statistics

import backend.common.model.BotOperation
import backend.db.bots.BotsDatabase
import java.time.Instant
import java.time.LocalTime

class StatisticsAggregator {

    private val botsDatabase = BotsDatabase()

    fun getBotReturn(
        botId: Int,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Result<Double> {
        val botOperations = getBotHistory(botId, startTimestamp, endTimestamp)
        var income = 0.0
        botOperations.getOrNull()?.forEach {
            income += it.returnValue
        }
        return Result.success(income)
    }

    fun getBotHistory(
        botId: Int,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Result<List<BotOperation>> {
        var operationsByBotId = botsDatabase.getOperations(botId).asSequence()
        startTimestamp?.let { timestamp ->
            operationsByBotId = operationsByBotId.filter { it.operationTime.toInstant() > timestamp }
        }
        endTimestamp?.let { timestamp ->
            operationsByBotId = operationsByBotId.filter { it.operationTime.toInstant() < timestamp }
        }

        val history = operationsByBotId.toList().map {
            val operationType = if (it.operationId == 0) {
                BotOperation.Type.BUY
            } else {
                BotOperation.Type.SELL
            }
            BotOperation(operationType, it.operationTime.toLocalDateTime(), it.price * it.count, 0.0)
        }
        return Result.success(history)
    }

    fun getStrategyReturnAverage(
        strategyId: Int,
        timestamp_from: Instant? = null,
        timestamp_to: Instant? = null
    ): Result<Double> {
        TODO()
    }

    fun getStrategyReturnHistory(
        strategyId: Int,
        period: LocalTime,
        timestamp_from: Instant? = null,
        timestamp_to: Instant? = null
    ): Result<List<backend.common.model.ReturnInfo>> {
        TODO()
    }
}
