package backend.bot.service

import backend.bot.BotCannotBeLoaded
import backend.bot.BotCluster
import backend.bot.BotName
import backend.bot.BotNotFoundException
import backend.bot.BotService
import backend.bot.BotUid
import backend.bot.HyperParameterCannotBeLoaded
import backend.common.model.BotInfo
import backend.common.model.Id
import backend.db.bots.BotsDatabase
import backend.strategy.StrategyService
import backend.strategy.StrategyUid
import backend.strategy.UnsupportedStrategyException

class DbBotService(
    private val db: BotsDatabase,
    configure: Configuration.() -> Unit
) : BotService {
    private val botClusters: Map<StrategyUid, BotCluster>
    private val strategyService: StrategyService
    private val bot2Cluster: MutableMap<BotUid, BotCluster> = mutableMapOf()

    init {
        val configuration = InternalConfiguration().apply(configure)

        strategyService = configuration.strategyService
        botClusters = configuration.botClusters

        loadBotsFromDb()
    }

    private fun loadBotsFromDb() {
        val strategyIds = strategyService.getStrategyIds().getOrThrow()

        for (strategyId in strategyIds) {
            val bots = db.getBotsByStrategy(strategyId)
            initializeBotsForStrategy(strategyId, bots)
        }
    }

    private fun initializeBotsForStrategy(strategyUid: StrategyUid, bots: List<BotUid>) {
        val strategyParams = strategyService.getStrategy(strategyUid).getOrThrow().parameterIds
        val strategyParamInfos = strategyParams.map { it to strategyService.getHyperParameter(it).getOrThrow() }

        for (botId in bots) {
            val name = db.getBotName(botId) ?: throw BotNotFoundException(botId)

            val parameters = strategyParamInfos.associate { (paramId, _) ->
                val paramValue = when (paramId) {
                    Id.figiHyperParameterUid -> db.getStringParameter(botId, paramId)
                    Id.balanceHyperParameterUid -> db.getDoubleParameter(botId, paramId)?.toString()
                    else -> db.getStringParameter(botId, paramId)
                }
                paramId to (paramValue ?: throw HyperParameterCannotBeLoaded(botId, paramId))
            }

            initBot(strategyUid, name, botId, parameters)
        }
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
        val uid = db.createBot(name, strategyUid) ?: return Result.failure(BotCannotBeLoaded(name, strategyUid))

        parameters.forEach { (paramId, value) ->
            when (paramId) {
                Id.figiHyperParameterUid -> db.setStringParameter(uid, paramId, value)
                Id.balanceHyperParameterUid -> db.setDoubleParameter(uid, paramId, value.toDouble())
                else -> db.setStringParameter(uid, paramId, value)
            }
        }

        return initBot(strategyUid, name, uid, parameters)
    }

    private fun initBot(
        strategyUid: StrategyUid,
        name: BotName,
        uid: Int,
        parameters: Map<Int, String>
    ): Result<BotUid> {
        val factory = strategyService.getStrategyContainerFactory(strategyUid).getOrElse { return Result.failure(it) }
        val cluster = botClusters[strategyUid] ?: return Result.failure(UnsupportedStrategyException(strategyUid))

        val container = factory.createStrategyController()

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