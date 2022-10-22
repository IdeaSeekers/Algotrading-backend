package backend.strategy.service

import backend.strategy.Strategy
import backend.strategy.StrategyContainerFactory
import backend.strategy.StrategyService
import backend.strategy.StrategyUid
import backend.strategy.UnsupportedStrategyException

class SimpleStrategyService(
    configure: Configuration.() -> Unit
) : StrategyService {
    private val strategies: Map<StrategyUid, Strategy>
    private val supportedFactories: Map<StrategyUid, StrategyContainerFactory>

    init {
        val configuration = InternalConfiguration().apply(configure)
        strategies = configuration.strategies
        supportedFactories = configuration.supportedFactories
    }


    override fun getStrategyContainerFactory(uid: StrategyUid): Result<StrategyContainerFactory> =
        supportedFactories[uid]?.let { Result.success(it) } ?: Result.failure(UnsupportedStrategyException(uid))

    override fun getStrategies(): List<StrategyUid> =
        strategies.values.map { it.uid }


    override fun getStrategy(uid: StrategyUid): Result<Strategy> =
        strategies[uid]?.let { Result.success(it) } ?: Result.failure(UnsupportedStrategyException(uid))

    interface Configuration {
        fun registerStrategy(strategy: Strategy, factory: StrategyContainerFactory)
    }

    // internal
    private class InternalConfiguration : Configuration {
        val strategies: MutableMap<StrategyUid, Strategy> = mutableMapOf()
        val supportedFactories: MutableMap<StrategyUid, StrategyContainerFactory> = mutableMapOf()

        override fun registerStrategy(strategy: Strategy, factory: StrategyContainerFactory) {
            val strategyUid = strategy.uid
            strategies[strategyUid] = strategy
            supportedFactories[strategyUid] = factory
        }
    }
}