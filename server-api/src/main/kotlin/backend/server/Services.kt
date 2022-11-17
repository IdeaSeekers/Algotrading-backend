package backend.server

import backend.bot.BotService
import backend.bot.clusters.SimpleCluster
import backend.bot.service.SimpleBotService
import backend.server.Info.balanceHyperParameterInfo
import backend.server.Info.figiHyperParameterInfo
import backend.server.Info.simpleStrategyInfo
import backend.strategy.StrategyService
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.simple.SimpleStrategyControllerFactory
import backend.strategy.strategies.simple.simpleStrategy
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService
import backend.tinkoff.account.TinkoffVirtualAccountFactory

object Services {

    val strategyService: StrategyService by lazy {
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

    val botService: BotService by lazy {
        SimpleBotService {
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

    // internal

    private const val token = "<token>" // TODO: handle invalid token

    private val tinkoffAccount by lazy {
        val sandboxService = TinkoffSandboxService(token)
        val accountId = sandboxService.createSandboxAccount().getOrThrow()
        sandboxService.sandboxPayIn(accountId, 1_000_000_u)
        TinkoffActualAccount(token, accountId)
    }
}