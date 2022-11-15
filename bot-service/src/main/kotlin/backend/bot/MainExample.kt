package backend.bot

import backend.bot.clusters.SimpleCluster
import backend.bot.service.SimpleBotService
import backend.common.model.StrategyInfo
import backend.strategy.Parameters
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.simple.SimpleStrategyControllerFactory
import backend.strategy.strategies.simple.simpleStrategy
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService

fun main() {
    val yandexStrategy = StrategyInfo(
        "Yandex securities",
        "Stupid strategy",
        StrategyInfo.Risk.HIGH,
        emptyList()
    )

    val strategyImpl = ::simpleStrategy

    val strategyService = SimpleStrategyService {
        registerStrategy(yandexStrategy, SimpleStrategyControllerFactory(strategyImpl))
    }

    val token = "<token>"

    val tinkoffAccount = TinkoffSandboxService(token).run service@{
        val id = createSandboxAccount().getOrThrow()
        sandboxPayIn(id, 100_000u)
        TinkoffActualAccount(token, id)
    }


    val service = SimpleBotService {
        withStrategyService(strategyService)
        withAccount(tinkoffAccount)

        val cluster = SimpleCluster()
        addCluster(0, cluster)
    }

    service.createBot("Sberbank", 0, 50_000.0, "BBG004730N88", Parameters(""))
    service.createBot( "Yandex", 0, 50_000.0, "BBG006L8G4H1", Parameters(""))
}