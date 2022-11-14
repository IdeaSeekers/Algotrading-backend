package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
data class GetBotsResponse(
    val bots: List<Bot>
) {
    @Serializable
    data class Bot(val id: Int)
}
