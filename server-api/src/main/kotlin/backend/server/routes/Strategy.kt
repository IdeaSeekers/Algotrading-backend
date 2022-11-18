package backend.server.routes

import backend.server.Services
import backend.server.util.badRequest
import backend.server.util.exception
import backend.server.response.GetActiveBotsCountResponse
import backend.server.response.GetStrategiesResponse
import backend.server.response.GetStrategyAverageReturnResponse
import backend.server.response.GetStrategyResponse
import backend.server.response.GetStrategyReturnHistoryResponse
import backend.server.util.parseId
import backend.server.util.parseTimestamp
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import java.time.LocalTime

fun Route.getStrategies() {
    get("") {
        Services.strategyService.getStrategyIds()
            .onSuccess { strategyIds ->
                val strategies = strategyIds.map { GetStrategiesResponse.Strategy(it) }
                call.respond(GetStrategiesResponse(strategies))
            }
            .onFailure {
                call.exception(it)
            }
    }
}

fun Route.getStrategy() {
    get("/{id}") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        Services.strategyService.getStrategy(id)
            .onSuccess { strategyInfo ->
                val strategy = GetStrategyResponse.fromStrategyInfo(strategyInfo)
                call.respond(strategy)
            }
            .onFailure {
                call.exception(it)
            }
    }
}

fun Route.getActiveBots() {
    get("/{id}/active") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        Services.botService.getRunningBotsCount(id)
            .onSuccess { botsCount ->
                val activeBots = GetActiveBotsCountResponse.BotsCount(botsCount)
                call.respond(GetActiveBotsCountResponse(activeBots))
            }
            .onFailure {
                call.exception(it)
            }
    }
}

fun Route.getAverageReturn() {
    get("/{id}/average_return") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        val timestampFrom = parseTimestamp(call.request.queryParameters["timestamp_from"])
        val timestampTo = parseTimestamp(call.request.queryParameters["timestamp_to"])

        Services.statisticsAggregator.getStrategyReturnAverage(id, timestampFrom, timestampTo)
            .onSuccess { averageReturn ->
                val averageReturnResponse = GetStrategyAverageReturnResponse(averageReturn)
                call.respond(averageReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }
    }
}

fun Route.getReturnHistory() {
    get("/{id}/return_history") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        Services.statisticsAggregator.getStrategyReturnHistory(id, LocalTime.of(0, 0, 1))
            .onSuccess { history ->
                val historyReturnResponse = GetStrategyReturnHistoryResponse.fromListReturnInfo(history)
                call.respond(historyReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }
    }
}
