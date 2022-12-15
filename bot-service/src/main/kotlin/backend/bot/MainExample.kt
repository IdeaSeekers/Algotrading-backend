package backend.bot

import backend.bot.clusters.SimpleCluster
import backend.bot.service.SimpleBotService
import backend.common.model.HyperParameterInfo
import backend.common.model.StrategyInfo
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.simple.SimpleStrategyControllerFactory
import backend.strategy.strategies.simple.simpleStrategy
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService
import backend.tinkoff.account.TinkoffVirtualAccountFactory

fun main() {
    val strategyImpl = ::simpleStrategy

    val figiParameterId = 0
    val balanceParameterId = 1
    val strategyId = 0
    val ownerUsername = "username"

    val strategyService = SimpleStrategyService {
        registerParameter(0, HyperParameterInfo("balance", "balance", HyperParameterInfo.Type.FLOAT, min = 0.0))
        registerParameter(1, HyperParameterInfo("figi", "figi", HyperParameterInfo.Type.STRING))

        val yandexStrategy = StrategyInfo(
            "Yandex securities",
            "Stupid strategy",
            StrategyInfo.Risk.HIGH,
            listOf(balanceParameterId, figiParameterId)
        )

        registerStrategy(0, yandexStrategy, SimpleStrategyControllerFactory(strategyImpl))
    }

    val token = "<token>"

    val tinkoffAccount = TinkoffSandboxService(token).run service@{
        val id = createSandboxAccount().getOrThrow()
        sandboxPayIn(id, 100_000u)
        TinkoffActualAccount(token, id)
    }
    val factory = TinkoffVirtualAccountFactory(tinkoffAccount)


    val service = SimpleBotService {
        withStrategyService(strategyService)

        val cluster = SimpleCluster(
            strategyId,
            balanceParameterId,
            figiParameterId,
            factory
        )
        addCluster(0, cluster)
    }

    val parameters1 = mapOf(
        balanceParameterId to "50_000.0",
        figiParameterId to "BBG004730N88",
    )

    service.createBot("Sberbank", strategyId, ownerUsername, parameters1)


    val parameters2 = mapOf(
        balanceParameterId to "50_000.0",
        figiParameterId to "BBG006L8G4H1",
    )

    service.createBot( "Yandex", strategyId, ownerUsername, parameters2)
}