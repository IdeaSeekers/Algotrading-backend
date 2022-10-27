package backend.strategy.strategies.stupid

import backend.strategy.Configuration
import backend.strategy.Status
import backend.strategy.StrategyContainer
import backend.tinkoff.account.TinkoffAccount
import backend.tinkoff.model.MarketPrice
import backend.tinkoff.model.OrderStatus
import backend.tinkoff.model.Quotation
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StupidStrategyContainer : StrategyContainer {
    private val executor = Executors.newSingleThreadExecutor()
    private val dispatcher = executor.asCoroutineDispatcher()
    private val scope = CoroutineScope(dispatcher)
    private lateinit var job: Job


    override fun start(configuration: Configuration): Boolean {
        job = scope.launch {
            strategy(configuration)
        }

        return true
    }

    override fun status(): Status {
        return when {
            scope.isActive -> Status.RUNNING
            job.isCompleted -> Status.STOPPED
            else -> Status.UNKNOWN
        }
    }

    override fun stop(): Boolean {
        scope.cancel()
        return true
    }

    override fun die(): Boolean {
        executor.shutdownNow()
        return true
    }

}

private suspend fun strategy(configuration: Configuration) {
    val account = configuration.tinkoffAccount
    val yandexFigi = "BBG006L8G4H1"
    var lastBuyPrice = Quotation.zero()

    while (currentCoroutineContext().isActive) {
        val (currencies, securities) = account.getPositions().getOrThrow()

        val balanceRub = currencies.firstOrNull { it.isoCode == "rub" }?.quotation ?: Quotation.zero()
        val yandex = securities.firstOrNull { it.figi == yandexFigi }
        fun f(q: Quotation) = "%8.2f".format(q.units.toDouble() + q.nano.toDouble() / 1e9)
        if (yandex != null)
            println("[${configuration.parameters.parameters}]    Balance: ${f(balanceRub)}₽    Yandex shares: ${yandex.balance}")
        else
            println("[${configuration.parameters.parameters}]    Balance: ${f(balanceRub)}₽")

        val yandexPrice = account.getLastPrice(yandexFigi).getOrThrow()


        val sell = yandex != null && yandex.balance > 0u && yandexPrice > lastBuyPrice

        if (sell) {
            account.postSellOrder(yandexFigi, 1u, MarketPrice).getOrThrow()

            continue
        }

        if (balanceRub < yandexPrice) {
            break
        }

        val responce = account.postBuyOrder(yandexFigi, 1u, MarketPrice).getOrThrow()

        if (responce.status == OrderStatus.FILL) {
            lastBuyPrice = responce.totalCost.quotation
        }

        delay(1000)
    }
}
