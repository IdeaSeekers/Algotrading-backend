package backend.bot.service

import backend.bot.*
import backend.common.model.BotInfo
import backend.strategy.Parameters
import backend.strategy.StrategyService
import backend.strategy.StrategyUid
import backend.strategy.UnsupportedStrategyException
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffVirtualAccount
import backend.tinkoff.account.TinkoffVirtualAccountFactory
import backend.tinkoff.model.Currency
import backend.tinkoff.model.Figi
import backend.tinkoff.model.Quotation

class SimpleBotService(
    configure: Configuration.() -> Unit
) : BotService {
    private val virtualAccountFactory: TinkoffVirtualAccountFactory
    private val botClusters: Map<StrategyUid, BotCluster>
    private val strategyService: StrategyService
    private val bot2Cluster: MutableMap<BotUid, BotCluster> = mutableMapOf()
    private val bot2Account: MutableMap<BotUid, TinkoffVirtualAccount> = mutableMapOf()

    private var botNumberer = 0

    init {
        val configuration = InternalConfiguration().apply(configure)

        virtualAccountFactory = TinkoffVirtualAccountFactory(configuration.tinkoffAccount)
        strategyService = configuration.strategyService
        botClusters = configuration.botClusters
    }

    override fun getBotIds(): Result<List<BotUid>> =
        botClusters.values
            .map { it.getBotIds() }
            .run { firstOrNull { it.isFailure } ?: Result.success(flatMap { it.getOrThrow() }) }

    override fun getBot(uid: BotUid): Result<BotInfo> {
        val cluster = bot2Cluster[uid] ?: return Result.failure(BotNotFoundException(uid))
        return cluster.getBot(uid)
    }

    override fun getRunningBotIds(): Result<List<BotUid>> =
        botClusters.values
            .map { it.getRunningBotIds() }
            .run { firstOrNull { it.isFailure } ?: Result.success(flatMap { it.getOrThrow() }) }

    override fun getRunningBotIds(strategyId: Int): Result<List<BotUid>> =
        botClusters.values
            .map { it.getRunningBotIds(strategyId) }
            .run { firstOrNull { it.isFailure } ?: Result.success(flatMap { it.getOrThrow() }) }

    override fun createBot(
        name: BotName,
        strategyUid: StrategyUid,
        initialBalance: Double,
        securityFigi: Figi,
        parameters: Parameters
    ): Result<BotUid> {
        val factory = strategyService.getStrategyContainerFactory(strategyUid).getOrElse { return Result.failure(it) }
        val cluster = botClusters[strategyUid] ?: return Result.failure(UnsupportedStrategyException(strategyUid))

        val container = factory.createStrategyController()

        val uid = botNumberer++

        val balance = Quotation(initialBalance.toUInt(), extractNanos(initialBalance))
        val virtualAccount = virtualAccountFactory.openVirtualAccount(
            uid,
            listOf(Currency("rub", balance))
        ).getOrElse { return Result.failure(it) }

        val result = cluster.deploy(
            container,
            virtualAccount,
            uid,
            name,
            strategyUid,
            securityFigi,
            parameters,
        )

        result.onSuccess {
            bot2Cluster[uid] = cluster
            bot2Account[uid] = virtualAccount
        }

        return result.map { uid }
    }

    override fun deleteBot(uid: BotUid): Result<Boolean> {
        val cluster = bot2Cluster[uid] ?: return Result.failure(BotNotFoundException(uid))
        val res = cluster.deleteBot(uid)
        if (res.isSuccess) {
            virtualAccountFactory.closeVirtualAccount(bot2Account.getValue(uid))
            bot2Cluster.remove(uid)
            bot2Account.remove(uid) // TODO: refactor
        }
        return res
    }

    override fun pauseBot(uid: BotUid): Result<Boolean> {
        val cluster = bot2Cluster[uid] ?: return Result.failure(BotNotFoundException(uid))
        val res = cluster.pauseBot(uid)
        return res
    }

    override fun resumeBot(uid: BotUid): Result<Boolean> {
        val cluster = bot2Cluster[uid] ?: return Result.failure(BotNotFoundException(uid))
        val res = cluster.pauseBot(uid)
        return res
    }

    interface Configuration {
        fun withAccount(tinkoffAccount: TinkoffActualAccount)
        fun withStrategyService(strategyService: StrategyService)
        fun addCluster(id: StrategyUid, cluster: BotCluster)
    }

    // internal
    private class InternalConfiguration : Configuration {
        lateinit var tinkoffAccount: TinkoffActualAccount
        lateinit var strategyService: StrategyService
        val botClusters: MutableMap<StrategyUid, BotCluster> = mutableMapOf()

        override fun withAccount(tinkoffAccount: TinkoffActualAccount) {
            this.tinkoffAccount = tinkoffAccount
        }

        override fun withStrategyService(strategyService: StrategyService) {
            this.strategyService = strategyService
        }

        override fun addCluster(id: StrategyUid, cluster: BotCluster) {
            botClusters[id] = cluster
        }
    }
}

private fun extractNanos(value: Double): UInt =
    ((value.toUInt().toDouble() - value) * 1e9).toUInt()