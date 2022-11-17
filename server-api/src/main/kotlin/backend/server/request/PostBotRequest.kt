package backend.server.request

import kotlinx.serialization.Serializable

@Serializable
data class PostBotRequest(
    val name: String,
    val strategy: Strategy,
    val initial_balance: Double,
    val security: String,
    val parameters: List<Parameter>
) {
    @Serializable
    data class Strategy(
        val id: Int
    )

    @Serializable
    data class Parameter(
        val id: Int,
        val value: Double,
    )
}
