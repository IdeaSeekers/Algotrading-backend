package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
data class GetStrategiesResponse(
    val strategies: List<Strategy>
) {
    @Serializable
    data class Strategy(val id: Int)
}
