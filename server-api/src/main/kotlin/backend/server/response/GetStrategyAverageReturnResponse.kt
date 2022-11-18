package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
data class GetStrategyAverageReturnResponse(
    val average_return: Double
)
