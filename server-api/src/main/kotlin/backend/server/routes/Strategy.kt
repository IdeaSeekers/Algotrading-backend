package backend.server.routes

import backend.server.Services
import backend.server.response.GetActiveBotsCountResponse
import backend.server.response.GetStrategiesResponse
import backend.server.response.GetStrategyAverageReturnResponse
import backend.server.response.GetStrategyResponse
import backend.server.response.GetStrategyReturnHistoryResponse
import backend.server.util.exception
import backend.server.util.parseTimestamp
import de.nielsfalk.ktor.swagger.example
import de.nielsfalk.ktor.swagger.get
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.responds
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.response.respond
import io.ktor.routing.Route
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.v
import java.time.LocalTime

private val logger = KotlinLogging.logger("StrategyRoutes")

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
        logger.info(
            "/strategy/",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
        )
    }
}

fun Route.getStrategy() {
    get<SwaggerStrategy>(
        "find".responds(
            ok<GetStrategyResponse>(example("model", SwaggerStrategy.responseExample)),
        )
    ) { params ->
        val id = params.id
        Services.strategyService.getStrategy(id)
            .onSuccess { strategyInfo ->
                val strategy = GetStrategyResponse.fromStrategyInfo(strategyInfo)
                call.respond(strategy)
            }
            .onFailure {
                call.exception(it)
            }
        logger.info(
            "/strategy/${id}",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}

fun Route.getActiveBots() {
    get<SwaggerActiveBots>(
        "count".responds(
            ok<SwaggerActiveBots>(example("model", SwaggerActiveBots.responseExample)),
        )
    ) { params ->
        val id = params.id
        Services.botService.getRunningBotsCount(id)
            .onSuccess { botsCount ->
                val activeBots = GetActiveBotsCountResponse.BotsCount(botsCount)
                call.respond(GetActiveBotsCountResponse(activeBots))
            }
            .onFailure {
                call.exception(it)
            }
        logger.info(
            "/strategy/$id/active",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
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

        val id = params.id
        Services.statisticsAggregator.getStrategyReturnAverage(id, timestampFrom, timestampTo)
            .onSuccess { averageReturn ->
                val averageReturnResponse = GetStrategyAverageReturnResponse(averageReturn)
                call.respond(averageReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }

        logger.info(
            "/strategy/$id/average_return",
            v("timestamp_from", timestampFrom),
            v("timestamp_to", timestampTo),
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}

fun Route.getReturnHistory() {
    get<SwaggerReturnHistory>(
        "history".responds(
            ok<SwaggerReturnHistory>(example("model", SwaggerReturnHistory.responseExample)),
        )
    ) { params ->
        val id = params.id
        Services.statisticsAggregator.getStrategyReturnHistory(id, LocalTime.of(0, 0, 1))
            .onSuccess { history ->
                val historyReturnResponse = GetStrategyReturnHistoryResponse.fromListReturnInfo(history)
                call.respond(historyReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }

        logger.info(
            "/strategy/$id/return_history",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}
