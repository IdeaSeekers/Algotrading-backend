package backend.strategy.service

import backend.common.model.StrategyInfo
import backend.strategy.*

class SimpleStrategyService(
    configure: Configuration.() -> Unit
) : StrategyService {
    private val strategies: Map<StrategyUid, StrategyInfo>
    private val supportedFactories: Map<StrategyUid, StrategyControllerFactory>

    init {
        val configuration = InternalConfiguration().apply(configure)
        strategies = configuration.strategies
        supportedFactories = configuration.supportedFactories
    }


    override fun getStrategyContainerFactory(uid: StrategyUid): Result<StrategyControllerFactory> =
        supportedFactories[uid]?.let { Result.success(it) } ?: Result.failure(UnsupportedStrategyException(uid))

    override fun getStrategyIds(): Result<List<StrategyUid>> =
            Result.success(strategies.keys.toList())


    override fun getStrategy(uid: StrategyUid): Result<StrategyInfo> =
        strategies[uid]?.let { Result.success(it) } ?: Result.failure(UnsupportedStrategyException(uid))

    override fun getRunningBotsCount(uid: StrategyUid): Result<Int> {
        return Result.success(227) // TODO
    }

    interface Configuration {
        fun registerStrategy(strategy: StrategyInfo, factory: StrategyControllerFactory)
    }

    // internal
    private class InternalConfiguration : Configuration {
        val strategies: MutableMap<StrategyUid, StrategyInfo> = mutableMapOf()
        val supportedFactories: MutableMap<StrategyUid, StrategyControllerFactory> = mutableMapOf()

        override fun registerStrategy(strategy: StrategyInfo, factory: StrategyControllerFactory) {
            val strategyUid = strategies.size
            strategies[strategyUid] = strategy
            supportedFactories[strategyUid] = factory
        }
    }
}