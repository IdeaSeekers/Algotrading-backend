package backend.server

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json

fun Application.module() {
    install(CORS) {
        anyHost()
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        allowHeaders { true }
        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    install(Routing)
    routes()
}

fun main() {
    embeddedServer(
        Netty, port = 8080,
        host = "127.0.0.1"
    ) {
        module()
    }.start(wait = true)
}