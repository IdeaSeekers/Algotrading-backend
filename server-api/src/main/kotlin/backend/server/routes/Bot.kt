package backend.server.routes

import backend.server.Services
import backend.server.util.badRequest
import backend.server.util.exception
import backend.server.request.PostBotRequest
import backend.server.request.PutBotRequest
import backend.server.response.GetBotResponse
import backend.server.response.GetBotsResponse
import backend.server.response.PostBotResponse
import backend.server.util.parseId
import backend.strategy.Parameters
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

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

        Services.botService.createBot(
            botInfo.name,
            botInfo.strategy.id,
            botInfo.initial_balance,
            botInfo.security,
            Parameters(botInfo.parameters.joinToString(", ") { "Parameter(${it.id}, ${it.value})" }) // TODO: HashMap<Int, Double>
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
        // call.request.queryParameters["timestamp_from"]
        // call.request.queryParameters["timestamp_to"]
        //
    }
}

fun Route.getOperations() {
    get("/{id}/operations") {
        //
    }
}
