package backend.server.routes

import backend.server.Services
import backend.server.response.GetSecuritiesResponse
import backend.server.util.exception
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.getSecurities() {
    get("") {
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