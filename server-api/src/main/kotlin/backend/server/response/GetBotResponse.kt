package backend.server.response

import backend.common.model.BotInfo
import kotlinx.serialization.Serializable

@Serializable
data class GetBotResponse(
    val name: String,
    val strategyId: Int,
    val balance: Double,
    val status: Status,
    val parameters: List<Parameter>,
) {
    @Serializable
    enum class Status {
        running,
        paused,
        stopped,
        unknown,
    }

    @Serializable
    data class Parameter(
        val id: Int,
        val value: String,
    )

    companion object {
        fun fromBotInfo(botInfo: BotInfo): GetBotResponse {
            val status = when (botInfo.status) {
                BotInfo.Status.RUNNING -> Status.running
                BotInfo.Status.PAUSED -> Status.paused
                BotInfo.Status.STOPPED -> Status.stopped
                BotInfo.Status.UNKNOWN -> Status.unknown
            }
            val parameters = botInfo.parameters.map {
                Parameter(it.id, it.value)
            }
            return GetBotResponse(
                botInfo.name,
                botInfo.strategyId,
                botInfo.balance,
                status,
                parameters
            )
        }
    }
}