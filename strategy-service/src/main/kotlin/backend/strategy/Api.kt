package backend.strategy

import backend.common.model.HyperParameterInfo
import backend.common.model.StrategyInfo

typealias StrategyUid = Int
typealias HyperParameterUid = Int

interface StrategyService {
    fun getStrategyContainerFactory(uid: StrategyUid): Result<StrategyControllerFactory>

    fun getStrategyIds(): Result<List<StrategyUid>>

    fun getStrategy(uid: StrategyUid): Result<StrategyInfo>

    fun getHyperParameter(uid: HyperParameterUid): Result<HyperParameterInfo>
}

