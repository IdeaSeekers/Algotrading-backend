package backend.server.routes

import backend.common.model.Id
import backend.server.Services
import backend.server.request.PostBotRequest
import backend.server.request.PutBotRequest
import backend.server.response.*
import backend.server.util.*
import de.nielsfalk.ktor.swagger.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getBots() {
    authenticate(JwtConfiguration.authName) {
        get<SwaggerBots>(
            "all".responds(
                ok<SwaggerBots>(example("model", SwaggerBots.responseExampleGet)),
            )
        ) {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.unauthorized()
            val username = principal.payload.getClaim(JwtConfiguration.fieldName).asString()
            val botService = Services.userService.getBotService(username)
                ?: return@get call.unauthorized()

            botService.getBotIds()
                .onSuccess { botIds ->
                    val bots = botIds.map { GetBotsResponse.Bot(it) }
                    call.respond(GetBotsResponse(bots))
                }
                .onFailure {
                    call.exception(it)
                }
        }
    }
}

fun Route.getBot() {
    authenticate(JwtConfiguration.authName) {
        get<SwaggerBot>(
            "find".responds(
                ok<SwaggerBot>(example("model", SwaggerBot.responseExampleGet)),
            )
        ) { params ->
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.unauthorized()
            val username = principal.payload.getClaim(JwtConfiguration.fieldName).asString()
            val botService = Services.userService.getBotService(username)
                ?: return@get call.unauthorized()

            botService.getBot(params.id)
                .onSuccess { botInfo ->
                    val bot = GetBotResponse.fromBotInfo(botInfo)
                    call.respond(bot)
                }
                .onFailure {
                    call.exception(it)
                }
        }
    }
}

fun Route.postBot() {
    authenticate(JwtConfiguration.authName) {
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

            val principal = call.principal<JWTPrincipal>()
                ?: return@post call.unauthorized()
            val username = principal.payload.getClaim(JwtConfiguration.fieldName).asString()
            val botService = Services.userService.getBotService(username)
                ?: return@post call.unauthorized()

            botService.createBot(
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
}

fun Route.deleteBot() {
    authenticate(JwtConfiguration.authName) {
        delete<SwaggerBot>(
            "delete".responds(
                ok<SwaggerBot>(example("model", SwaggerBot.responseExampleDelete)),
            )
        ) { params ->
            val principal = call.principal<JWTPrincipal>()
                ?: return@delete call.unauthorized()
            val username = principal.payload.getClaim(JwtConfiguration.fieldName).asString()
            val botService = Services.userService.getBotService(username)
                ?: return@delete call.unauthorized()

            botService.deleteBot(params.id)
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
}

fun Route.putBot() {
    authenticate(JwtConfiguration.authName) {
        put<SwaggerBot, PutBotRequest>(
            "switch".responds(
                ok<SwaggerBot>(example("model", SwaggerBot.responseExamplePut)),
            )
        ) { params, putBotRequest ->
            val principal = call.principal<JWTPrincipal>()
                ?: return@put call.unauthorized()
            val username = principal.payload.getClaim(JwtConfiguration.fieldName).asString()
            val botService = Services.userService.getBotService(username)
                ?: return@put call.unauthorized()

            val operationResult = when (putBotRequest.status) {
                PutBotRequest.Status.running -> botService.resumeBot(params.id)
                PutBotRequest.Status.paused -> botService.pauseBot(params.id)
            }
            operationResult.onFailure {
                call.exception(it)
            }
        }
    }
}

fun Route.getReturn() {
    authenticate(JwtConfiguration.authName) {
        get<SwaggerBotReturn>(
            "return".responds(
                ok<SwaggerBotReturn>(example("model", SwaggerBotReturn.responseExample)),
            )
        ) { params ->
            call.principal<JWTPrincipal>() ?: return@get call.unauthorized()
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
}

fun Route.getOperations() {
    authenticate(JwtConfiguration.authName) {
        get<SwaggerBotReturn>(
            "history".responds(
                ok<SwaggerBotOperations>(example("model", SwaggerBotOperations.responseExample)),
            )
        ) { params ->
            call.principal<JWTPrincipal>() ?: return@get call.unauthorized()
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
}
