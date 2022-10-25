package backend.bot.api

import backend.strategy.Parameters
import backend.strategy.Status
import backend.strategy.StrategyContainer
import backend.strategy.StrategyUid
import backend.tinkoff.account.TinkoffAccount

typealias BotUid = Int

typealias BotName = String

data class Bot(
    val uid: BotUid,
    val name: BotName,
    val parameters: Parameters,
    val status: Status,
)

interface BotService {
    fun activeBots(): List<BotUid>
    fun getBot(uid: BotUid): Result<Bot>
    fun startBot(strategyUid: StrategyUid, name: BotName, parameters: Parameters): Result<BotUid>
    fun stopBot(uid: BotUid): Boolean
}

interface BotCluster {
    fun activeBots(): List<BotUid>
    fun getBot(uid: BotUid): Result<Bot>
    fun deploy(container: StrategyContainer, name: BotName, parameters: Parameters, tinkoffAccount: TinkoffAccount): Result<BotUid>
    fun stopBot(uid: BotUid): Boolean
}