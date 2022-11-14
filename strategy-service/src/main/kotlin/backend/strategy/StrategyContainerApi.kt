package backend.strategy

import backend.common.model.BotInfo.Status
import backend.tinkoff.account.TinkoffAccount

interface StrategyControllerFactory {
    fun createStrategyController(): StrategyController
}

interface StrategyController {
    fun start(configuration: Configuration): Result<Boolean>
    fun status(): Result<Status>
    fun pause(): Result<Boolean>
    fun delete(): Result<Boolean>

    fun resume(): Result<Boolean>
    fun balance(): Result<Double>
}

data class Configuration(
    val tinkoffAccount: TinkoffAccount,
    val parameters: Parameters,
    val figi: String,
)