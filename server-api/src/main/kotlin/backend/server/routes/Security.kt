package backend.server.routes

import backend.server.Services
import backend.server.response.GetSecuritiesResponse
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

private val logger = KotlinLogging.logger("SecurityRoutes")

fun Route.getSecurities() {
    get<SwaggerSecurities>(
        "find".responds(
            ok<SwaggerSecurities>(example("model", SwaggerSecurities.responseExample))
        )
    ) {
        Services.tinkoffInfoService.getSecurities()
            .onSuccess { securities ->
                val securitiesResponse = GetSecuritiesResponse.fromSecurityInfo(securities)
                call.respond(securitiesResponse)
            }
            .onFailure {
                call.exception(it)
            }
        logger.info("/security/",
            v("REST", "GET"),
            v("remote", call.request.origin.host),
            v("response", call.response.status())
        )
    }
}