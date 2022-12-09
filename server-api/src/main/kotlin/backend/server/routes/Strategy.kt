package backend.server.routes

import backend.server.Services
import backend.server.response.*
import backend.server.util.JwtConfiguration
import backend.server.util.exception
import backend.server.util.parseTimestamp
import backend.server.util.unauthorized
import de.nielsfalk.ktor.swagger.example
import de.nielsfalk.ktor.swagger.get
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.responds
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*
import java.time.LocalTime

fun Route.getStrategies() {
    get<SwaggerStrategies>(
        "all".responds(
            ok<GetStrategiesResponse>(example("model", SwaggerStrategies.responseExample))
        )
    ) {
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
    get<SwaggerStrategy>(
        "find".responds(
            ok<GetStrategyResponse>(example("model", SwaggerStrategy.responseExample)),
        )
    ) { params ->
        Services.strategyService.getStrategy(params.id)
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
    authenticate(JwtConfiguration.authName) {
        get<SwaggerActiveBots>(
            "count".responds(
                ok<SwaggerActiveBots>(example("model", SwaggerActiveBots.responseExample)),
            )
        ) { params ->
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.unauthorized()
            val username = principal.payload.getClaim(JwtConfiguration.fieldName).asString()
            val botService = Services.userService.getBotService(username)
                ?: return@get call.unauthorized()

            botService.getRunningBotsCount(params.id)
                .onSuccess { botsCount ->
                    val activeBots = GetActiveBotsCountResponse.BotsCount(botsCount)
                    call.respond(GetActiveBotsCountResponse(activeBots))
                }
                .onFailure {
                    call.exception(it)
                }
        }
    }
}

fun Route.getAverageReturn() {
    get<SwaggerAverageReturn>(
        "return".responds(
            ok<SwaggerAverageReturn>(example("model", SwaggerAverageReturn.responseExample)),
        )
    ) { params ->
        val timestampFrom = parseTimestamp(call.request.queryParameters["timestamp_from"])
        val timestampTo = parseTimestamp(call.request.queryParameters["timestamp_to"])

        Services.statisticsAggregator.getStrategyReturnAverage(params.id, timestampFrom, timestampTo)
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
    get<SwaggerReturnHistory>(
        "history".responds(
            ok<SwaggerReturnHistory>(example("model", SwaggerReturnHistory.responseExample)),
        )
    ) { params ->
        Services.statisticsAggregator.getStrategyReturnHistory(params.id, LocalTime.of(0, 0, 1))
            .onSuccess { history ->
                val historyReturnResponse = GetStrategyReturnHistoryResponse.fromListReturnInfo(history)
                call.respond(historyReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }
    }
}
