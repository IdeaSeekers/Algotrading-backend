package backend.strategy.strategies.simple

import backend.strategy.Configuration
import backend.strategy.StrategyController
import backend.strategy.StrategyControllerFactory

class SimpleStrategyControllerFactory(
    private val strategy: suspend (Configuration) -> Unit
) : StrategyControllerFactory {
    override fun createStrategyController(): StrategyController {
        return SimpleStrategyContainer(strategy)
    }
}