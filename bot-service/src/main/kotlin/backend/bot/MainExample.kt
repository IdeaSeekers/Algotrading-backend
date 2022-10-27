package backend.bot

import backend.bot.clusters.SimpleCluster
import backend.bot.service.SimpleBotService
import backend.strategy.Parameters
import backend.strategy.ParametersDescription
import backend.strategy.Strategy
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.megastupid.MegaStupidStrategyContainerFactory
import backend.strategy.strategies.stupid.StupidStrategyContainerFactory
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService

fun main() {
    val yandexStrategy = Strategy(
        1,
        "Yandex securities",
        "Stupid strategy",
        ParametersDescription("No parameters")
    )

    val sberbankStrategy = Strategy(
        2,
        "Sberbank securities",
        "Mega stupid strategy",
        ParametersDescription("No parameters")
    )

    val strategyService = SimpleStrategyService {
        registerStrategy(yandexStrategy, StupidStrategyContainerFactory())
        registerStrategy(sberbankStrategy, MegaStupidStrategyContainerFactory())
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
        addCluster(1, cluster)
        addCluster(2, cluster) // both strategies start on the same cluster
    }

    service.startBot(1, "TestBot1", Parameters(""))
    service.startBot(2, "TestBot2", Parameters(""))
}