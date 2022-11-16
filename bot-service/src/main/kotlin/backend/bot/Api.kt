package backend.bot

import backend.common.model.BotInfo
import backend.strategy.Parameters
import backend.strategy.StrategyController
import backend.strategy.StrategyUid
import backend.tinkoff.account.TinkoffAccount
import backend.tinkoff.model.Figi

typealias BotUid = Int

typealias BotName = String

interface BotService {
    fun getBotIds(): Result<List<BotUid>>

    fun getBot(uid: BotUid): Result<BotInfo>

    fun createBot(
        name: BotName,
        strategyUid: StrategyUid,
        initialBalance: Double,
        securityFigi: Figi,
        parameters: Parameters
    ): Result<BotUid>

    fun deleteBot(uid: BotUid): Result<Boolean>

    fun pauseBot(uid: BotUid): Result<Boolean>

    fun resumeBot(uid: BotUid): Result<Boolean>

    fun getRunningBotIds(): Result<List<BotUid>>

    fun getRunningBotIds(strategyId: Int): Result<List<BotUid>>
}

interface BotCluster {
    fun getBotIds(): Result<List<BotUid>>

    fun getBot(uid: BotUid): Result<BotInfo>

    fun deleteBot(uid: BotUid): Result<Boolean>

    fun pauseBot(uid: BotUid): Result<Boolean>

    fun resumeBot(uid: BotUid): Result<Boolean>

    fun getRunningBotIds(): Result<List<BotUid>>

    fun getRunningBotIds(strategyId: Int): Result<List<BotUid>>

    fun deploy(
        controller: StrategyController,
        tinkoffAccount: TinkoffAccount,
        uid: BotUid,
        name: BotName,
        strategyUid: StrategyUid,
        securityFigi: Figi,
        parameters: Parameters,
    ): Result<Boolean>
}