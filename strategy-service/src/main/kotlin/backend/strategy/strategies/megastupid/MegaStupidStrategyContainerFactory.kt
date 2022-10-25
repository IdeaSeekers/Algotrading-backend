package backend.strategy.strategies.megastupid

import backend.strategy.StrategyContainer
import backend.strategy.StrategyContainerFactory
import backend.strategy.strategies.MegaStupidStrategyContainer

class MegaStupidStrategyContainerFactory : StrategyContainerFactory {
    override fun createStrategyContainer(): StrategyContainer {
        return MegaStupidStrategyContainer()
    }
}