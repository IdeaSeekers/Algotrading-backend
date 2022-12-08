package backend.server.routes

import backend.server.Services
import backend.server.response.GetParameterResponse
import backend.server.util.exception
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

private val logger = KotlinLogging.logger("ParameterRoutes")

fun Route.getParameter() {
    get<SwaggerParameter>(
        "all".responds(
            ok<SwaggerParameter>(example("model", SwaggerParameter.responseExample))
        )
    ) { params ->
        val id = params.id
        Services.strategyService.getHyperParameter(id)
            .onSuccess { hyperParameterInfo ->
                val bot = GetParameterResponse.fromHyperParameterInfo(hyperParameterInfo)
                call.respond(bot)
            }
            .onFailure {
                call.exception(it)
            }

        logger.info("/parameter/$id",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status()),
            v("id", id)
        )
    }
}
