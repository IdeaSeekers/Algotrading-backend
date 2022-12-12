package backend.server

import backend.server.routes.routes
import backend.server.util.JwtConfiguration
import backend.server.util.unauthorized
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import de.nielsfalk.ktor.swagger.SwaggerSupport
import de.nielsfalk.ktor.swagger.version.shared.Contact
import de.nielsfalk.ktor.swagger.version.shared.Information
import de.nielsfalk.ktor.swagger.version.v2.Swagger
import de.nielsfalk.ktor.swagger.version.v3.OpenApi
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun Application.module() {
    install(CORS) {
        anyHost()
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        allowHeaders { true }
        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true
    }
    install(DefaultHeaders)
    install(Compression)
    install(CallLogging)
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(Locations)
    install(SwaggerSupport) {
        forwardRoot = true
        val information = Information(
            version = "0.1",
            title = "Algotrading-backend API",
            contact = Contact(
                name = "GitHub",
                url = "https://github.com/IdeaSeekers/Algotrading-backend/"
            )
        )
        swagger = Swagger().apply {
            info = information
        }
        openApi = OpenApi().apply {
            info = information
            security = listOf(mapOf("basic" to listOf("bearer")))
            components.securitySchemes["basic"] = mapOf(
                "type" to "http",
                "scheme" to "bearer",
                "bearerFormat" to "JWT",
            )
        }
    }
    install(Authentication) {
        jwt(JwtConfiguration.authName) {
            realm = JwtConfiguration.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(JwtConfiguration.secret))
                    .withAudience(JwtConfiguration.audience)
                    .withIssuer(JwtConfiguration.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim(JwtConfiguration.fieldName).asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.unauthorized()
            }
        }
    }
    // install(Routing)
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