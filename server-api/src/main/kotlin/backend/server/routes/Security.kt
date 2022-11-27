package backend.server.routes

import backend.server.Services
import backend.server.response.GetSecuritiesResponse
import backend.server.util.exception
import de.nielsfalk.ktor.swagger.example
import de.nielsfalk.ktor.swagger.get
import de.nielsfalk.ktor.swagger.ok
import de.nielsfalk.ktor.swagger.responds
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

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
    }
}