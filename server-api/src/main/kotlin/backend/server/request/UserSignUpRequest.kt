package backend.server.request

import backend.common.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserSignUpRequest(
    val username: String,
    val password: String,
    val tinkoff: String // token
) {
    fun toUser() = User(username, password, tinkoff)
}
