package backend.server.routes

import backend.server.Services
import backend.server.request.UserSignInRequest
import backend.server.request.UserSignUpRequest
import backend.server.response.UserAmountResponse
import backend.server.response.UserSignInResponse
import backend.server.util.JwtConfiguration
import backend.server.util.exception
import backend.server.util.unauthorized
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import de.nielsfalk.ktor.swagger.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*

fun Route.userSignUp() {
    post<SwaggerUserSignUp, UserSignUpRequest>(
        "register".examples(
            example("amogus", SwaggerUserSignUp.requestExample)
        ).responds(
            ok<SwaggerUserSignUp>(example("ok", SwaggerUserSignUp.responseExample))
        )
    ) { _, user ->
        Services.userService.addUser(user.toUser())
            .onFailure { call.exception(it) }
            .onSuccess { call.respond(HttpStatusCode.OK) }
    }
}

fun Route.userSignIn() {
    post<SwaggerUserSignIn, UserSignInRequest>(
        "sign-in".examples(
            example("amogus", SwaggerUserSignIn.requestExample)
        ).responds(
            ok<SwaggerUserSignIn>(example("tokens", SwaggerUserSignIn.responseExample))
        )
    ) { _, userRequest ->
        val maybeUser = Services.userService.loginUser(userRequest.username, userRequest.password)

        if (maybeUser == null) {
            call.respond(HttpStatusCode.NotFound, "User not found")
            return@post
        }

        val jwtToken = JWT.create()
            .withAudience(JwtConfiguration.audience)
            .withIssuer(JwtConfiguration.issuer)
            .withClaim(JwtConfiguration.fieldName, userRequest.username)
            .withExpiresAt(Date(System.currentTimeMillis() + JwtConfiguration.expirationTimeMs))
            .sign(Algorithm.HMAC256(JwtConfiguration.secret))

        val userResponse = UserSignInResponse(jwtToken, maybeUser.tinkoff)
        call.respond(userResponse)
    }
}

fun Route.getUserAmount() {
    authenticate(JwtConfiguration.authName) {
        get<SwaggerUserAmount>(
            "get".responds(
                ok<SwaggerUserAmount>(example("amount", SwaggerUserAmount.responseExample))
            )
        ) {
            val principal = call.principal<JWTPrincipal>()
                ?: return@get call.unauthorized()
            val username = principal.payload.getClaim(JwtConfiguration.fieldName).asString()

            val tinkoffAccount = Services.userService.getTinkoffAccount(username)
                ?: return@get call.unauthorized()
            val positions = tinkoffAccount.getPositions()
                .getOrElse { return@get call.exception(it) }

            val rubles = positions.currencies.firstOrNull { it.isoCode == "rub" }?.quotation?.toDouble() ?: 0.0
            call.respond(UserAmountResponse(rubles))
        }
    }
}
