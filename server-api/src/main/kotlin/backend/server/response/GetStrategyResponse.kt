package backend.server.response

import backend.common.model.StrategyInfo
import kotlinx.serialization.Serializable

@Serializable
data class GetStrategyResponse(
    val name: String,
    val description: String,
    val risk: Risk,
    val parameters: List<Parameter>
) {
    @Serializable
    enum class Risk {
        low,
        medium,
        high,
    }

    @Serializable
    data class Parameter(
        val id: Int
    )

    companion object {
        fun fromStrategyInfo(strategyInfo: StrategyInfo): GetStrategyResponse {
            val risk = when (strategyInfo.risk) {
                StrategyInfo.Risk.LOW -> Risk.low
                StrategyInfo.Risk.MEDIUM -> Risk.medium
                StrategyInfo.Risk.HIGH -> Risk.high
            }
            val parameters = strategyInfo.parameterIds.map {
                Parameter(it)
            }
            return GetStrategyResponse(strategyInfo.name, strategyInfo.description, risk, parameters)
        }
    }
}
