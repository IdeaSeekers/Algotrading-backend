package backend.server.response

import backend.common.model.BotInfo
import backend.server.Services
import backend.common.model.Id
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
        val value: Double,
    )

    companion object {
        fun fromBotInfo(botInfo: BotInfo): GetBotResponse {
            val status = when (botInfo.status) {
                BotInfo.Status.RUNNING -> Status.running
                BotInfo.Status.PAUSED -> Status.paused
                BotInfo.Status.STOPPED -> Status.stopped
                BotInfo.Status.UNKNOWN -> Status.unknown
            }
            val parameters = botInfo.parameters.map {parameter ->
                if (parameter.id == Id.figiHyperParameterUid) {
                    val securityId = Services.tinkoffInfoService.getIdByFigi(parameter.value).getOrThrow()
                    Parameter(parameter.id, securityId.toDouble())
                } else {
                    Parameter(parameter.id, parameter.value.toDouble())
                }
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