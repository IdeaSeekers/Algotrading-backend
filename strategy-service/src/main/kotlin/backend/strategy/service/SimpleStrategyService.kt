package backend.strategy.service

import backend.common.model.HyperParameterInfo
import backend.common.model.StrategyInfo
import backend.strategy.HyperParameterUid
import backend.strategy.StrategyControllerFactory
import backend.strategy.StrategyService
import backend.strategy.StrategyUid
import backend.strategy.UnknownHyperParameterException
import backend.strategy.UnsupportedStrategyException

class SimpleStrategyService(
    configure: Configuration.() -> Unit
) : StrategyService {
    private val strategies: Map<StrategyUid, StrategyInfo>
    private val supportedFactories: Map<StrategyUid, StrategyControllerFactory>
    private val hyperParameters: Map<HyperParameterUid, HyperParameterInfo>

    init {
        val configuration = InternalConfiguration().apply(configure)
        strategies = configuration.strategies
        supportedFactories = configuration.supportedFactories
        hyperParameters = configuration.hyperParameters
    }


    override fun getStrategyContainerFactory(uid: StrategyUid): Result<StrategyControllerFactory> =
        supportedFactories[uid]?.let { Result.success(it) } ?: Result.failure(UnsupportedStrategyException(uid))

    override fun getStrategyIds(): Result<List<StrategyUid>> =
            Result.success(strategies.keys.toList())


    override fun getStrategy(uid: StrategyUid): Result<StrategyInfo> =
        strategies[uid]?.let { Result.success(it) } ?: Result.failure(UnsupportedStrategyException(uid))

    override fun getHyperParameter(uid: HyperParameterUid): Result<HyperParameterInfo> =
        hyperParameters[uid]?.let { Result.success(it) } ?: Result.failure(UnknownHyperParameterException(uid))

    interface Configuration {
        fun registerStrategy(uid: StrategyUid, strategy: StrategyInfo, factory: StrategyControllerFactory): StrategyUid
        fun registerParameter(uid: HyperParameterUid, parameterInfo: HyperParameterInfo): Int
    }

    // internal
    private class InternalConfiguration : Configuration {
        val hyperParameters: MutableMap<HyperParameterUid, HyperParameterInfo> = mutableMapOf()
        val strategies: MutableMap<StrategyUid, StrategyInfo> = mutableMapOf()
        val supportedFactories: MutableMap<StrategyUid, StrategyControllerFactory> = mutableMapOf()
        override fun registerParameter(uid: HyperParameterUid, parameterInfo: HyperParameterInfo): Int {
            hyperParameters[uid] = parameterInfo
            return uid
        }

        override fun registerStrategy(uid: StrategyUid, strategy: StrategyInfo, factory: StrategyControllerFactory): StrategyUid {
            strategies[uid] = strategy
            supportedFactories[uid] = factory
            return uid
        }
    }
}