package backend.server.routes

import backend.server.Services
import backend.server.util.badRequest
import backend.server.util.exception
import backend.server.response.GetActiveBotsCountResponse
import backend.server.response.GetStrategiesResponse
import backend.server.response.GetStrategyResponse
import backend.server.util.parseId
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

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

        Services.strategyService.getRunningBotsCount(id)
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
        // call.request.queryParameters["timestamp_from"]
        // call.request.queryParameters["timestamp_to"]
        //
    }
}

fun Route.getReturnHistory() {
    get("/{id}/return_history") {
        //
    }
}
