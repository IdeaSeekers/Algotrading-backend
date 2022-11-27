package backend.server.routes

import backend.server.Id
import backend.server.Services
import backend.server.request.PostBotRequest
import backend.server.request.PutBotRequest
import backend.server.response.*
import backend.server.util.badRequest
import backend.server.util.exception
import backend.server.util.parseTimestamp
import de.nielsfalk.ktor.swagger.*
import io.ktor.application.*
import io.ktor.http.HttpStatusCode
import io.ktor.response.*
import io.ktor.routing.*

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
    }
}

fun Route.getBot() {
    get<SwaggerBot>(
        "find".responds(
            ok<SwaggerBot>(example("model", SwaggerBot.responseExampleGet)),
        )
    ) { params ->
        Services.botService.getBot(params.id)
            .onSuccess { botInfo ->
                val bot = GetBotResponse.fromBotInfo(botInfo)
                call.respond(bot)
            }
            .onFailure {
                call.exception(it)
            }
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
    }
}

fun Route.deleteBot() {
    delete<SwaggerBot>(
        "delete".responds(
            ok<SwaggerBot>(example("model", SwaggerBot.responseExampleDelete)),
        )
    ) { params ->
        Services.botService.deleteBot(params.id)
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
    }
}

fun Route.putBot() {
    put<SwaggerBot, PutBotRequest>(
        "switch".responds(
            ok<SwaggerBot>(example("model", SwaggerBot.responseExamplePut)),
        )
    ) { params, putBotRequest ->
        val operationResult = when (putBotRequest.status) {
            PutBotRequest.Status.running -> Services.botService.resumeBot(params.id)
            PutBotRequest.Status.paused -> Services.botService.pauseBot(params.id)
        }
        operationResult.onFailure {
            call.exception(it)
        }
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

        Services.statisticsAggregator.getBotReturn(params.id, timestampFrom, timestampTo)
            .onSuccess { botReturn ->
                val botReturnResponse = GetBotReturnResponse(botReturn)
                call.respond(botReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }
    }
}

fun Route.getOperations() {
    get<SwaggerBotReturn>(
        "history".responds(
            ok<SwaggerBotOperations>(example("model", SwaggerBotOperations.responseExample)),
        )
    ) { params ->
        Services.statisticsAggregator.getBotHistory(params.id)
            .onSuccess { botHistory ->
                val botReturnResponse = GetBotHistoryResponse.fromListOperation(botHistory)
                call.respond(botReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }
    }
}
