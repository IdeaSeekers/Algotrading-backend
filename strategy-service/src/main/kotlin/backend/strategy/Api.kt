package backend.strategy

import backend.common.model.StrategyInfo

typealias StrategyUid = Int

data class ParametersDescription(
    val description: String
)

data class Parameters(
    val parameters: String
)

interface StrategyService {
    fun getStrategyContainerFactory(uid: StrategyUid): Result<StrategyControllerFactory>

    fun getStrategyIds(): Result<List<StrategyUid>>

    fun getStrategy(uid: StrategyUid): Result<StrategyInfo>
}

