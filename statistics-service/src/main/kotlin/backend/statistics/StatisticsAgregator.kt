package backend.statistics

import backend.common.model.BotOperation
import backend.common.model.Id
import backend.common.model.ReturnInfo
import backend.db.bots.BotsDatabase
import java.time.*

class StatisticsAggregator(
    private val botsDatabase: BotsDatabase
) {

    fun getBotReturn(
        botId: Int,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Result<Double> {
        val botOperations = getBotHistory(botId, startTimestamp, endTimestamp)
            .getOrElse { return Result.failure(it) }

        return when (botOperations.size) {
            0 -> Result.success(0.0)
            1 -> Result.success(botOperations.last().returnValue)
            else -> {
                val firstReturnValue = botOperations.first().returnValue
                val lastReturnValue = botOperations.last().returnValue
                Result.success(lastReturnValue - firstReturnValue)
            }
        }
    }

    fun getBotHistory(
        botId: Int,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Result<List<BotOperation>> {
        val initialBalance = botsDatabase.getDoubleParameter(botId, Id.balanceHyperParameterUid)
            ?: return Result.failure(Exception("No parameter with id ${Id.balanceHyperParameterUid} in DB"))

        val startInstant = startTimestamp ?: Instant.MIN
        val endInstant = endTimestamp ?: Instant.MAX
        val operations = botsDatabase.getOperations(botId).filter {
            it.operationTime.toLocalDateTime().toInstant(ZoneOffset.UTC) in startInstant..endInstant
        }

        val history = operations.map {
            BotOperation(
                type = BotOperation.Type.values()[it.operationId - 1],
                timestamp = it.operationTime.toLocalDateTime(),
                executedPrice = it.price * it.count,
                returnValue = it.botBalance - initialBalance
            )
        }
        return Result.success(history)
    }

    fun getStrategyReturnAverage(
        strategyId: Int,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Result<Double> {
        val strategyBots = botsDatabase.getBotsByStrategy(strategyId).toSet()
        val botsReturn = strategyBots.mapNotNull { botId ->
            getBotReturn(botId, startTimestamp, endTimestamp).getOrNull()
        }
        return Result.success(botsReturn.average())
    }

    fun getStrategyReturnHistory(
        strategyId: Int,
        period: LocalTime,
        startTimestamp: Instant? = null,
        endTimestamp: Instant? = null
    ): Result<List<ReturnInfo>> {
        val strategyBots = botsDatabase.getBotsByStrategy(strategyId).toSet()
        val botsHistory = strategyBots.mapNotNull { botId ->
            getBotHistory(botId, startTimestamp, endTimestamp).getOrNull()
        }
        if (botsHistory.isEmpty()) {
            return Result.success(listOf())
        }

        val botsNotEmptyHistory = botsHistory.filter { it.isNotEmpty() }
        val startTime = startTimestamp ?: botsNotEmptyHistory.minOf { operations ->
            operations.minOf { it.timestamp }.toInstant(ZoneOffset.UTC).minusSeconds(1)
        }
        val endTime = endTimestamp ?: botsNotEmptyHistory.maxOf { operations ->
            operations.maxOf { it.timestamp }.toInstant(ZoneOffset.UTC).plusSeconds(1)
        }
        val periodNanos = period.toNanoOfDay()

        val returnHistory = mutableListOf<ReturnInfo>()

        var currentTime = startTime
        while (currentTime <= endTime.plusNanos(periodNanos)) {
            val botsReturnValues = botsHistory.map { operations ->
                val currentOperation = operations.lastOrNull {
                    val operationTime = it.timestamp.toInstant(ZoneOffset.UTC)
                    operationTime < currentTime
                }
                currentOperation?.returnValue ?: 0.0
            }
            returnHistory += ReturnInfo(
                LocalDateTime.ofInstant(currentTime, ZoneOffset.UTC),
                botsReturnValues.average()
            )
            currentTime = currentTime.plusNanos(periodNanos)
        }

        return Result.success(returnHistory)
    }
}
