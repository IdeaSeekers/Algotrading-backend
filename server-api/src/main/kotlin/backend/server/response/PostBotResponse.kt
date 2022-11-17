package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
data class PostBotResponse(
    val bot: Bot
) {
    @Serializable
    data class Bot(val id: Int)
}
