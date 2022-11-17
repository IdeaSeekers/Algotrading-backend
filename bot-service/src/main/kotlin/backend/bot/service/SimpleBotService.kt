package backend.bot.service

import backend.bot.BotCluster
import backend.bot.BotName
import backend.bot.BotNotFoundException
import backend.bot.BotService
import backend.bot.BotUid
import backend.common.model.BotInfo
import backend.strategy.StrategyService
import backend.strategy.StrategyUid
import backend.strategy.UnsupportedStrategyException

class SimpleBotService(
    configure: Configuration.() -> Unit
) : BotService {
    private val botClusters: Map<StrategyUid, BotCluster>
    private val strategyService: StrategyService
    private val bot2Cluster: MutableMap<BotUid, BotCluster> = mutableMapOf()

    private var botNumberer = 0

    init {
        val configuration = InternalConfiguration().apply(configure)

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

    override fun getRunningBotIds(uid: StrategyUid): Result<List<BotUid>> =
        botClusters.values
            .map { it.getRunningBotIds(uid) }
            .run { firstOrNull { it.isFailure } ?: Result.success(flatMap { it.getOrThrow() }) }

    override fun getRunningBotsCount(uid: StrategyUid): Result<Int> =
        getRunningBotIds(uid).map { it.size }

    override fun createBot(
        name: BotName,
        strategyUid: StrategyUid,
        parameters: Map<Int, String>
    ): Result<BotUid> {
        val factory = strategyService.getStrategyContainerFactory(strategyUid).getOrElse { return Result.failure(it) }
        val cluster = botClusters[strategyUid] ?: return Result.failure(UnsupportedStrategyException(strategyUid))

        val container = factory.createStrategyController()

        val uid = botNumberer++

        val result = cluster.deploy(
            container,
            name,
            uid,
            parameters,
        )

        result.onSuccess {
            bot2Cluster[uid] = cluster
        }

        return result.map { uid }
    }

    override fun deleteBot(uid: BotUid): Result<Boolean> {
        val cluster = bot2Cluster[uid] ?: return Result.failure(BotNotFoundException(uid))
        val res = cluster.deleteBot(uid)
        if (res.isSuccess) {
            bot2Cluster.remove(uid)
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
        fun withStrategyService(strategyService: StrategyService)
        fun addCluster(id: StrategyUid, cluster: BotCluster)
    }

    // internal
    private class InternalConfiguration : Configuration {
        lateinit var strategyService: StrategyService
        val botClusters: MutableMap<StrategyUid, BotCluster> = mutableMapOf()

        override fun withStrategyService(strategyService: StrategyService) {
            this.strategyService = strategyService
        }

        override fun addCluster(id: StrategyUid, cluster: BotCluster) {
            botClusters[id] = cluster
        }
    }
}