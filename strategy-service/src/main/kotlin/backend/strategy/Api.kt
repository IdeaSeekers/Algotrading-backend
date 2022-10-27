package backend.strategy

typealias StrategyUid = Int

data class Strategy(
    val uid: StrategyUid,
    val name: String,
    val description: String,
    val parameters: ParametersDescription
)

data class ParametersDescription(
    val description: String
)

data class Parameters(
    val parameters: String
)

interface StrategyService {
    fun getStrategyContainerFactory(uid: StrategyUid): Result<StrategyContainerFactory>

    fun getStrategies(): List<StrategyUid>

    fun getStrategy(uid: StrategyUid): Result<Strategy>
}

