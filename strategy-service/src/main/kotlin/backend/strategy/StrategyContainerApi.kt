package backend.strategy

import backend.tinkoff.account.TinkoffAccount

interface StrategyContainerFactory {
    fun createStrategyContainer(): StrategyContainer
}

interface StrategyContainer {
    fun start(configuration: Configuration): Boolean
    fun status(): Status
    fun stop(): Boolean
    fun die(): Boolean
}

data class Configuration(
    val parameters: Parameters,
    val tinkoffAccount: TinkoffAccount,
)

enum class Status {
    RUNNING, STOPPED, UNKNOWN,
}
