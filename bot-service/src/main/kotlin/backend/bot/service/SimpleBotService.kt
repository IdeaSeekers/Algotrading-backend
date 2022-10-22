package backend.bot.service

import backend.bot.BotNotFoundException
import backend.bot.api.Bot
import backend.bot.api.BotCluster
import backend.bot.api.BotName
import backend.bot.api.BotService
import backend.bot.api.BotUid
import backend.strategy.Parameters
import backend.strategy.StrategyService
import backend.strategy.StrategyUid
import backend.strategy.UnsupportedStrategyException
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffVirtualAccountFactory
import backend.tinkoff.model.Currency
import backend.tinkoff.model.Quotation
import java.util.concurrent.atomic.AtomicInteger

class SimpleBotService(
    configure: Configuration.() -> Unit
) : BotService {
    private val virtualAccoutnFactory: TinkoffVirtualAccountFactory
    private val botClusters: Map<StrategyUid, BotCluster>
    private val strategyService: StrategyService
    private val bot2Cluster: MutableMap<BotUid, BotCluster> = mutableMapOf()


    init {
        val configuration = InternalConfiguration().apply(configure)

        virtualAccoutnFactory = TinkoffVirtualAccountFactory(configuration.tinkoffAccount)
        strategyService = configuration.strategyService
        botClusters = configuration.botClusters
    }

    override fun activeBots(): List<BotUid> =
        botClusters.values.flatMap { it.activeBots() }


    override fun getBot(uid: BotUid): Result<Bot> =
        bot2Cluster[uid]?.getBot(uid) ?: Result.failure(BotNotFoundException(uid))

    override fun startBot(strategyUid: StrategyUid, name: BotName, parameters: Parameters): Result<BotUid> {
        val factory = strategyService.getStrategyContainerFactory(strategyUid).getOrElse { return Result.failure(it) }
        val cluster = botClusters[strategyUid] ?: return Result.failure(UnsupportedStrategyException(strategyUid))

        val container = factory.createStrategyContainer()

        val virtualAccount = virtualAccoutnFactory.openVirtualAccount(
            listOf(Currency("rub", Quotation(1000u, 0u)))
        ).getOrElse { return Result.failure(it) }

        val result = cluster.deploy(container, name, parameters, virtualAccount)

        result.onSuccess {
            bot2Cluster[it] = cluster
        }

        return result
    }

    override fun stopBot(uid: BotUid): Boolean {
        val cluster = bot2Cluster[uid] ?: return false
        val res = cluster.stopBot(uid)
        if (res) {
            bot2Cluster.clear()
        }
        return res
    }


    interface Configuration {
        fun withAccount(tinkoffAccount: TinkoffActualAccount)
        fun withStrategyService(strategyService: StrategyService)
        fun addCluster(id: StrategyUid, cluster: BotCluster)

        val synchronizer: AtomicInteger
    }

    // internal
    private class InternalConfiguration : Configuration {
        override val synchronizer: AtomicInteger = AtomicInteger()

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