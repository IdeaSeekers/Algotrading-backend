package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
data class UserSignInResponse(
    val jwt: String,
    val tinkoff: String
)
