package backend.server.routes

import backend.server.Id
import backend.server.Services
import backend.server.request.PostBotRequest
import backend.server.request.PutBotRequest
import backend.server.response.GetBotHistoryResponse
import backend.server.response.GetBotResponse
import backend.server.response.GetBotReturnResponse
import backend.server.response.GetBotsResponse
import backend.server.response.PostBotResponse
import backend.server.util.badRequest
import backend.server.util.exception
import backend.server.util.parseTimestamp
import de.nielsfalk.ktor.swagger.*
import io.ktor.application.*
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.response.*
import io.ktor.routing.*
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.v

private val logger = KotlinLogging.logger("BotRoutes")

fun Route.getBots() {
    get<SwaggerBots>(
        "all".responds(
            ok<SwaggerBots>(example("model", SwaggerBots.responseExampleGet)),
        )
    ) {
        Services.botService.getBotIds()
            .onSuccess { botIds ->
                val bots = botIds.map { GetBotsResponse.Bot(it) }
                call.respond(GetBotsResponse(bots))
            }
            .onFailure {
                call.exception(it)
            }
        logger.info("/bot/",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status())
        )
    }
}

fun Route.getBot() {
    get<SwaggerBot>(
        "find".responds(
            ok<SwaggerBot>(example("model", SwaggerBot.responseExampleGet)),
        )
    ) { params ->
        val id = params.id
        Services.botService.getBot(id)
            .onSuccess { botInfo ->
                val bot = GetBotResponse.fromBotInfo(botInfo)
                call.respond(bot)
            }
            .onFailure {
                call.exception(it)
            }
        logger.info("/bot/$id",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}

fun Route.postBot() {
    post<SwaggerBots, PostBotRequest>(
        "new".examples(
            example("bot1", SwaggerBots.requestExamplePost)
        ).responds(
            ok<SwaggerBots>(example("model", SwaggerBots.responseExamplePost))
        )
    ) { _, botInfo ->
        val securityId = botInfo.parameters[Id.figiHyperParameterUid].value.toInt()
        val securityFigi = Services.tinkoffInfoService.getFigiById(securityId)
            .onFailure { return@post call.badRequest(it.message) }
            .getOrThrow()

        val parameters = botInfo.parameters.associate { parameter ->
            if (parameter.id == Id.figiHyperParameterUid)
                parameter.id to securityFigi
            else
                parameter.id to parameter.value.toString()
        }
        // these parameters are added explicitly

        Services.botService.createBot(
            botInfo.name,
            botInfo.strategy.id,
            parameters
        )
            .onSuccess { botId ->
                val bot = PostBotResponse.Bot(botId)
                call.respond(PostBotResponse(bot))
            }
            .onFailure {
                call.exception(it)
            }
        call.application.environment.log.info("hello")

        logger.info("/bot/",
            v("REST", "POST"),
            v("remote", call.request.origin.host),
            v("response", call.response.status())
        )
    }
}

fun Route.deleteBot() {
    delete<SwaggerBot>(
        "delete".responds(
            ok<SwaggerBot>(example("model", SwaggerBot.responseExampleDelete)),
        )
    ) { params ->
        val id = params.id
        Services.botService.deleteBot(id)
            .onSuccess { successful ->
                if (successful) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            .onFailure {
                call.exception(it)
            }
        logger.info("/delete/$id",
            v("REST", "POST"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}

fun Route.putBot() {
    put<SwaggerBot, PutBotRequest>(
        "switch".responds(
            ok<SwaggerBot>(example("model", SwaggerBot.responseExamplePut)),
        )
    ) { params, putBotRequest ->
        val id = params.id
        val operationResult = when (putBotRequest.status) {
            PutBotRequest.Status.running -> Services.botService.resumeBot(id)
            PutBotRequest.Status.paused -> Services.botService.pauseBot(id)
        }
        operationResult.onFailure {
            call.exception(it)
        }
        logger.info("/bot/$id",
            v("REST", "PUT"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}

fun Route.getReturn() {
    get<SwaggerBotReturn>(
        "return".responds(
            ok<SwaggerBotReturn>(example("model", SwaggerBotReturn.responseExample)),
        )
    ) { params ->
        val timestampFrom = parseTimestamp(call.request.queryParameters["timestamp_from"])
        val timestampTo = parseTimestamp(call.request.queryParameters["timestamp_to"])

        val id = params.id
        Services.statisticsAggregator.getBotReturn(id, timestampFrom, timestampTo)
            .onSuccess { botReturn ->
                val botReturnResponse = GetBotReturnResponse(botReturn)
                call.respond(botReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }

        logger.info("/bot/$id/return",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )

    }
}

fun Route.getOperations() {
    get<SwaggerBotReturn>(
        "history".responds(
            ok<SwaggerBotOperations>(example("model", SwaggerBotOperations.responseExample)),
        )
    ) { params ->
        val id = params.id
        Services.statisticsAggregator.getBotHistory(id)
            .onSuccess { botHistory ->
                val botReturnResponse = GetBotHistoryResponse.fromListOperation(botHistory)
                call.respond(botReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }

        logger.info("/bot/$id/operations",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}
