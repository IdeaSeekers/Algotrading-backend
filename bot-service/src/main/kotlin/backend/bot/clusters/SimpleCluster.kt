package backend.bot.clusters

import backend.bot.BotCluster
import backend.bot.BotName
import backend.bot.BotNotFoundException
import backend.bot.BotUid
import backend.common.model.BotInfo
import backend.strategy.Configuration
import backend.strategy.Parameters
import backend.strategy.StrategyController
import backend.strategy.StrategyUid
import backend.tinkoff.account.TinkoffAccount
import backend.tinkoff.model.Figi

class SimpleCluster : BotCluster {
    private data class BotWrapper(
        val info: BotBaseInfo,
        val container: StrategyController,
    )

    private val bots: MutableMap<BotUid, BotWrapper> = mutableMapOf()

    override fun getRunningBotIds(): Result<List<BotUid>> =
        Result.success(bots.keys.filter { getBot(it).getOrNull()?.status == BotInfo.Status.RUNNING })

    override fun getBotIds(): Result<List<BotUid>> =
        Result.success(bots.keys.toList())

    override fun getBot(uid: BotUid): Result<BotInfo> {
        val wrapper = bots[uid] ?: return Result.failure(BotNotFoundException(uid))
        val status = wrapper.container.status().getOrElse { return Result.failure(it) }
        val balance = wrapper.container.balance().getOrElse { return Result.failure(it) }
        val info = BotInfo(
            wrapper.info.name,
            wrapper.info.strategyId,
            balance, // TODO: total balance
            wrapper.info.securityFigi,
            status,
            wrapper.info.parameters
        )
        return Result.success(info)
    }

    override fun deleteBot(uid: BotUid): Result<Boolean> {
        val wrapper = bots[uid] ?: return Result.failure(BotNotFoundException(uid))
        val result = wrapper.container.delete()
        if (result.isSuccess) {
            bots.remove(uid)
        }
        return result
    }

    override fun pauseBot(uid: BotUid): Result<Boolean> {
        val wrapper = bots[uid] ?: return Result.failure(BotNotFoundException(uid))
        return wrapper.container.pause()
    }

    override fun resumeBot(uid: BotUid): Result<Boolean> {
        val wrapper = bots[uid] ?: return Result.failure(BotNotFoundException(uid))
        return wrapper.container.resume()
    }

    override fun deploy(
        container: StrategyController,
        tinkoffAccount: TinkoffAccount,
        uid: BotUid,
        name: BotName,
        strategyUid: StrategyUid,
        securityFigi: Figi,
        parameters: Parameters,
    ): Result<Boolean> {
        val configuration = Configuration(
            tinkoffAccount,
            parameters,
            securityFigi
        )
        val res = container.start(configuration).getOrElse { return Result.failure(it) }

        if (res) {
            val baseInfo = BotBaseInfo(
                name,
                strategyUid,
                securityFigi,
                emptyList()
            )

            bots[uid] = BotWrapper(baseInfo, container)
        }

        return Result.success(res)
    }
}