package backend.server

import backend.bot.BotService
import backend.bot.clusters.SimpleCluster
import backend.bot.service.SimpleBotService
import backend.common.model.StrategyInfo
import backend.strategy.StrategyService
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.simple.SimpleStrategyControllerFactory
import backend.strategy.strategies.simple.simpleStrategy
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService

object Services {

    val strategyService: StrategyService by lazy {
        val strategyInfo = StrategyInfo(
            "Simple strategy",
            "Super mega hyper ultra stupid strategy",
            StrategyInfo.Risk.HIGH,
            emptyList()
        )
        SimpleStrategyService {
            registerStrategy(strategyInfo, SimpleStrategyControllerFactory(::simpleStrategy))
        }
    }

    val botService: BotService by lazy {
        SimpleBotService {
            withStrategyService(strategyService)
            withAccount(tinkoffAccount)

            val cluster = SimpleCluster()
            addCluster(id = 0, cluster)
        }
    }

    // internal

    private const val token = "<token>" // TODO: handle invalid token

    private val tinkoffAccount by lazy {
        val sandboxService = TinkoffSandboxService(token)
        val accountId = sandboxService.createSandboxAccount().getOrThrow()
        sandboxService.sandboxPayIn(accountId, 1_000_000_u)
        TinkoffActualAccount(token, accountId)
    }
}