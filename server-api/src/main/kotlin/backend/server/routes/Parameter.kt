package backend.server.routes

import backend.server.Services
import backend.server.response.GetParameterResponse
import backend.server.util.badRequest
import backend.server.util.exception
import backend.server.util.parseId
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get


fun Route.getParameter() {
    get("/{id}") {
        val id = parseId(call.parameters["id"])
            .onFailure { return@get call.badRequest(it.message) }
            .getOrThrow()

        Services.strategyService.getHyperParameter(id)
            .onSuccess { hyperParameterInfo ->
                val bot = GetParameterResponse.fromHyperParameterInfo(hyperParameterInfo)
                call.respond(bot)
            }
            .onFailure {
                call.exception(it)
            }
    }
}
