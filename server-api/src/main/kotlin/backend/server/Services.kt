package backend.server

import backend.bot.BotService
import backend.bot.clusters.SimpleCluster
import backend.bot.service.DbBotService
import backend.db.bots.BotsDatabase
import backend.common.model.Id
import backend.common.model.Info.balanceHyperParameterInfo
import backend.common.model.Info.figiHyperParameterInfo
import backend.common.model.Info.simpleStrategyInfo
import backend.statistics.StatisticsAggregator
import backend.strategy.StrategyService
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.simple.SimpleStrategyControllerFactory
import backend.strategy.strategies.simple.simpleStrategy
import backend.tinkoff.service.TinkoffInfoService
import backend.user.UserService

object Services {

    private val botsDatabase = BotsDatabase()

    val strategyService: StrategyService = run {
        SimpleStrategyService {
            registerParameter(Id.figiHyperParameterUid, figiHyperParameterInfo)
            registerParameter(Id.balanceHyperParameterUid, balanceHyperParameterInfo)
            registerStrategy(
                Id.simpleStrategyUid,
                simpleStrategyInfo,
                SimpleStrategyControllerFactory(::simpleStrategy)
            )
        }
    }

    val userService: UserService = UserService(strategyService)
    // internal

    private val tinkoffAccount = run {
        val sandboxService = TinkoffSandboxService(token)
        val accountId = sandboxService.createSandboxAccount().getOrThrow()
        sandboxService.sandboxPayIn(accountId, 1_000_000_u)
        TinkoffActualAccount(token, accountId)
    }

    val botService: BotService = run { // we need to load bots from the db, so we can't compute it lazily
        DbBotService(
            botsDatabase
        ) {
            withStrategyService(strategyService)
            val tinkoffAccountFactory = TinkoffVirtualAccountFactory(tinkoffAccount)
            val cluster = SimpleCluster(
                Id.simpleStrategyUid,
                Id.balanceHyperParameterUid,
                Id.figiHyperParameterUid,
                tinkoffAccountFactory
            )
            addCluster(Id.simpleStrategyUid, cluster)
        }
    }

    val tinkoffInfoService: TinkoffInfoService = TinkoffInfoService()

    val statisticsAggregator: StatisticsAggregator = StatisticsAggregator()
}
    val statisticsAggregator: StatisticsAggregator = StatisticsAggregator(botsDatabase)

    // internal

    private const val token = "<token>" // TODO: handle invalid token
}