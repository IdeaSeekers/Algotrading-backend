package backend.statistics

import backend.common.model.BotOperation
import backend.common.model.ReturnInfo
import backend.db.bots.BotsDatabase
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

class StatisticsAggregator(
    private val botsDatabase: BotsDatabase
) {

    fun getBotReturn(
        botId: Int,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Result<Double> {
        val botOperationsResult = getBotHistory(botId, startTimestamp, endTimestamp)
        val lastBotBalance = botOperationsResult.getOrNull()?.lastOrNull()?.returnValue ?: 0.0
        val initialBalance = botsDatabase.getDoubleParameter(botId,1)
            ?: return Result.failure(Exception("No operation result"))

        if (lastBotBalance.isFinite().not() || initialBalance.isFinite().not()) {
            return Result.failure(Exception("Incorrect initial or current balance"))
        }

        return Result.success(lastBotBalance - initialBalance)
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
            BotOperation(
                BotOperation.Type.values()[it.operationId],
                it.operationTime.toLocalDateTime(),
                it.price * it.count,
                0.0 // TOOD(mandelshtamd): add operation return
            )
        }
        return Result.success(history)
    }

    fun getStrategyReturnAverage(
        strategyId: Int,
        timestamp_from: Instant? = null,
        timestamp_to: Instant? = null
    ): Result<Double> {
        val strategyBots = botsDatabase.getBotsByStrategy(strategyId).toSet()
        val botsReturn = strategyBots.mapNotNull { botId ->
            getBotReturn(botId, timestamp_from, timestamp_to).getOrNull()
        }

        val averageReturn = if (botsReturn.isNotEmpty()) {
            botsReturn.reduce { returnSum, botReturn -> returnSum + botReturn } / botsReturn.size
        } else {
            0.0
        }

        return Result.success(averageReturn)
    }

    fun getStrategyReturnHistory(
        strategyId: Int,
        period: LocalTime,
        timestamp_from: Instant = Instant.parse(serviceStartTime),
        timestamp_to: Instant = Instant.now()
    ): Result<List<ReturnInfo>> {
        val returnInfos = mutableListOf<ReturnInfo>()

        var currentPeriod = timestamp_from
        while (currentPeriod < timestamp_to) {
            val nextPeriod = currentPeriod.plusNanos(period.toNanoOfDay())
            val returnAtPeriod = getStrategyReturnAverage(strategyId, currentPeriod, nextPeriod).getOrNull()
            returnAtPeriod?.let {
                returnInfos.add(
                    ReturnInfo(
                        LocalDateTime.ofInstant(currentPeriod, ZoneOffset.UTC),
                        returnAtPeriod
                    )
                )
            }
            currentPeriod = nextPeriod
        }
        return Result.success(returnInfos)
    }

    companion object {
        private const val serviceStartTime = "2022-12-10T05:00:00Z"
    }
}
