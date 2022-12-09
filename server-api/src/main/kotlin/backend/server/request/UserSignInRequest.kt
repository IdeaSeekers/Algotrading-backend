package backend.server.request

import kotlinx.serialization.Serializable

@Serializable
data class UserSignInRequest(
    val username: String,
    val password: String,
)
