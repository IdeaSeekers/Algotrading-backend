package backend.bot.clusters

import backend.bot.BotCluster
import backend.bot.BotNotFoundException
import backend.bot.BotUid
import backend.bot.ExpectedHyperParameterException
import backend.bot.ParseHyperParameterException
import backend.bot.util.toDouble
import backend.common.model.BotInfo
import backend.common.model.BotInfo.Status
import backend.strategy.Configuration
import backend.strategy.HyperParameterUid
import backend.strategy.StrategyController
import backend.strategy.StrategyUid
import backend.tinkoff.account.TinkoffVirtualAccount
import backend.tinkoff.account.TinkoffVirtualAccountFactory
import backend.tinkoff.model.Currency
import backend.tinkoff.model.Quotation

class SimpleCluster(
    private val strategyUid: StrategyUid,
    private val balanceParameterUid: HyperParameterUid,
    private val figiParameterUid: HyperParameterUid,
    private val tinkoffFactory: TinkoffVirtualAccountFactory,
) : BotCluster {

    private data class BotWrapper(
        val info: BotBaseInfo,
        val controller: StrategyController,
        val virtualAccount: TinkoffVirtualAccount,
    )

    private val bots: MutableMap<BotUid, BotWrapper> = mutableMapOf()

    override fun getRunningBotIds(): Result<List<BotUid>> =
        Result.success(bots.keys.filter { bot ->
            getBot(bot).getOrNull()?.status == Status.RUNNING
        })

    override fun getRunningBotIds(strategyId: Int): Result<List<BotUid>> =
        Result.success(bots.keys.filter { bot ->
            getBot(bot).getOrNull()?.run { this.status == Status.RUNNING && this.strategyId == strategyId } ?: false
        })

    override fun getBotIds(): Result<List<BotUid>> =
        Result.success(bots.keys.toList())

    override fun getBot(uid: BotUid): Result<BotInfo> {
        val wrapper = bots[uid]
            ?: return Result.failure(BotNotFoundException(uid))
        val status = wrapper.controller.status()
            .getOrElse { return Result.failure(it) }
        val balance = wrapper.virtualAccount.getTotalBalance().map { it.toDouble() }
            .getOrElse { return Result.failure(it) }
        val info = BotInfo(
            wrapper.info.name,
            wrapper.info.strategyId,
            balance,
            status,
            wrapper.info.parameters
        )
        return Result.success(info)
    }

    override fun deleteBot(uid: BotUid): Result<Boolean> {
        val wrapper = bots[uid] ?: return Result.failure(BotNotFoundException(uid))
        val result = wrapper.controller.delete()
        if (result.isSuccess) {
            bots.remove(uid)?.apply { tinkoffFactory.closeVirtualAccount(virtualAccount) }
        }
        return result
    }

    override fun pauseBot(uid: BotUid): Result<Boolean> {
        val wrapper = bots[uid] ?: return Result.failure(BotNotFoundException(uid))
        return wrapper.controller.pause()
    }

    override fun resumeBot(uid: BotUid): Result<Boolean> {
        val wrapper = bots[uid] ?: return Result.failure(BotNotFoundException(uid))
        return wrapper.controller.resume()
    }

    override fun deploy(
        controller: StrategyController,
        name: String,
        uid: BotUid,
        parameters: Map<Int, String>
    ): Result<Boolean> {
        val initialBalanceStr =
            parameters[balanceParameterUid]
                ?: return Result.failure(ExpectedHyperParameterException(balanceParameterUid))
        val initialBalance = initialBalanceStr.toDoubleOrNull() ?: return Result.failure(
            ParseHyperParameterException(
                balanceParameterUid,
                initialBalanceStr
            )
        )
        val figi = parameters[figiParameterUid]
            ?: return Result.failure(ExpectedHyperParameterException(figiParameterUid))

        val balance = Quotation(initialBalance.toUInt(), extractNanos(initialBalance))
        val virtualAccount = tinkoffFactory.openVirtualAccount(
            uid,
            listOf(Currency("rub", balance))
        ).getOrElse { return Result.failure(it) }

        val configuration = Configuration(
            virtualAccount,
            figi,
            parameters,
        )

        val res = controller.start(configuration).getOrElse { return Result.failure(it) }

        if (res) {
            val baseInfo = BotBaseInfo(
                name,
                strategyUid,
                parameters.map { BotInfo.Parameter(it.key, it.value) }
            )

            bots[uid] = BotWrapper(
                baseInfo,
                controller,
                virtualAccount,
            )
        }

        return Result.success(res)
    }
}

private fun extractNanos(value: Double): UInt =
    ((value.toUInt().toDouble() - value) * 1e9).toUInt()