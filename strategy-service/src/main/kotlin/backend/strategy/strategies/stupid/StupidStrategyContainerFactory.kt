package backend.strategy.strategies.stupid

import backend.strategy.StrategyContainer
import backend.strategy.StrategyContainerFactory

class StupidStrategyContainerFactory : StrategyContainerFactory {
    override fun createStrategyContainer(): StrategyContainer {
        return StupidStrategyContainer()
    }
}