package backend.server.response

import kotlinx.serialization.Serializable

@Serializable
class GetBotReturnResponse(
    val `return`: Double
)
