package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
data class GetActiveBotsCountResponse(
    val bots: BotsCount
) {
    @Serializable
    data class BotsCount(val count: Int)
}
