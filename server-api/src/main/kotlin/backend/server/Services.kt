package backend.server

import backend.common.model.Id
import backend.common.model.Info.balanceHyperParameterInfo
import backend.common.model.Info.figiHyperParameterInfo
import backend.common.model.Info.simpleStrategyInfo
import backend.db.bots.BotsDatabase
import backend.statistics.StatisticsAggregator
import backend.strategy.StrategyService
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.simple.SimpleStrategyControllerFactory
import backend.strategy.strategies.simple.simpleStrategy
import backend.tinkoff.service.TinkoffInfoService
import backend.user.UserService

object Services {

    private val botsDatabase = BotsDatabase()

    val strategyService: StrategyService = SimpleStrategyService {
        registerParameter(Id.figiHyperParameterUid, figiHyperParameterInfo)
        registerParameter(Id.balanceHyperParameterUid, balanceHyperParameterInfo)
        registerStrategy(
            Id.simpleStrategyUid,
            simpleStrategyInfo,
            SimpleStrategyControllerFactory(::simpleStrategy)
        )
    }

    val userService: UserService = UserService(botsDatabase, strategyService)

    val tinkoffInfoService: TinkoffInfoService = TinkoffInfoService()

    val statisticsAggregator: StatisticsAggregator = StatisticsAggregator(botsDatabase)
}