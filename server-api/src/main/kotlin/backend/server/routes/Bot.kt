package backend.server.routes

import backend.server.Id
import backend.server.Services
import backend.server.request.PostBotRequest
import backend.server.request.PutBotRequest
import backend.server.response.*
import backend.server.util.badRequest
import backend.server.util.exception
import backend.server.util.parseId
import backend.server.util.parseTimestamp
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.put

fun Route.getBots() {
    get("") {
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
    get("/{id}") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        Services.botService.getBot(id)
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
    post("") {
        val botInfo = try {
            call.receive<PostBotRequest>()
        } catch (e: Throwable) {
            return@post call.exception(e)
        }

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
    delete("/{id}") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@delete call.badRequest(it.message) }
            .getOrThrow()

        Services.botService.deleteBot(id)
            .onFailure {
                call.exception(it)
            }
    }
}

fun Route.putBot() {
    put("/{id}") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@put call.badRequest(it.message) }
            .getOrThrow()

        val putBotRequest = try {
            call.receive<PutBotRequest>()
        } catch (e: Throwable) {
            return@put call.exception(e)
        }

        val operationResult = when (putBotRequest.status) {
            PutBotRequest.Status.running -> Services.botService.resumeBot(id)
            PutBotRequest.Status.paused -> Services.botService.pauseBot(id)
        }
        operationResult.onFailure {
            call.exception(it)
        }
    }
}

fun Route.getReturn() {
    get("/{id}/return") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        val timestampFrom = parseTimestamp(call.request.queryParameters["timestamp_from"])
        val timestampTo = parseTimestamp(call.request.queryParameters["timestamp_to"])

        Services.statisticsAggregator.getBotReturn(id, timestampFrom, timestampTo)
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
    get("/{id}/operations") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        Services.statisticsAggregator.getBotHistory(id)
            .onSuccess { botHistory ->
                val botReturnResponse = GetBotHistoryResponse.fromListOperation(botHistory)
                call.respond(botReturnResponse)
            }
            .onFailure {
                call.exception(it)
            }
    }
}
