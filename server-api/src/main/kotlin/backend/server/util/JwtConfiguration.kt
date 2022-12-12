package backend.server.util

object JwtConfiguration {
    const val secret = "secret" // TODO
    const val issuer = "http://127.0.0.1:8080/"
    const val audience = "http://127.0.0.1:8080/user/signin"
    const val realm = "Access to server"

    const val authName = "auth-jwt"
    const val fieldName = "username"

    const val expirationTimeMs = 10 * 60 * 1000
}