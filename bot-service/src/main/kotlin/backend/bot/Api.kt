package backend.bot

import backend.common.model.BotInfo
import backend.strategy.StrategyController
import backend.strategy.StrategyUid

typealias BotUid = Int
typealias BotName = String

interface BotService {
    fun getBotIds(): Result<List<BotUid>>

    fun getBot(uid: BotUid): Result<BotInfo>

    fun createBot(
        name: BotName,
        strategyUid: StrategyUid,
        ownerUsername: String,
        parameters: Map<Int, String>
    ): Result<BotUid>

    fun deleteBot(uid: BotUid): Result<Boolean>

    fun pauseBot(uid: BotUid): Result<Boolean>

    fun resumeBot(uid: BotUid): Result<Boolean>

    fun getRunningBotIds(): Result<List<BotUid>>

    fun getRunningBotIds(uid: StrategyUid): Result<List<BotUid>>

    fun getRunningBotsCount(uid: StrategyUid): Result<Int>
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
        name: String,
        uid: BotUid,
        parameters: Map<Int, String>
    ): Result<Boolean>
}