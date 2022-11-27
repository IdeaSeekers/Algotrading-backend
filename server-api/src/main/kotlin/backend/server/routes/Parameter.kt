package backend.server.routes

import backend.server.Services
import backend.server.response.GetParameterResponse
import backend.server.util.exception
import de.nielsfalk.ktor.swagger.example
import de.nielsfalk.ktor.swagger.get
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.responds
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getParameter() {
    get<SwaggerParameter>(
        "all".responds(
            ok<SwaggerParameter>(example("model", SwaggerParameter.responseExample))
        )
    ) { params ->
        Services.strategyService.getHyperParameter(params.id)
            .onSuccess { hyperParameterInfo ->
                val bot = GetParameterResponse.fromHyperParameterInfo(hyperParameterInfo)
                call.respond(bot)
            }
            .onFailure {
                call.exception(it)
            }
    }
}
