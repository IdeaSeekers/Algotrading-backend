package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
data class UserAmountResponse(
    val amount: Double
)
