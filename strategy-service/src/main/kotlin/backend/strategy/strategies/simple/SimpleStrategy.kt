package backend.strategy.strategies.simple

import backend.strategy.Configuration
import backend.tinkoff.model.MarketPrice
import backend.tinkoff.model.OrderStatus
import backend.tinkoff.model.Quotation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

suspend fun simpleStrategy(config: Configuration, balanceHandler: BalanceHandler) {
    val account = config.tinkoffAccount
    val figi = config.figi

    var lastBuyPrice = Quotation.zero()

    while (currentCoroutineContext().isActive) {

        val (currencies, securities) = account.getPositions().getOrThrow()

        val balanceRub = currencies.firstOrNull { it.isoCode == "rub" }?.quotation ?: Quotation.zero()
        val security = securities.firstOrNull { it.figi == figi }

        balanceHandler.balance.set(balanceRub.units.toDouble() + balanceRub.nano.toDouble() * 1e-9)


        val yandexPrice = account.getLastPrice("BBG006L8G4H1").getOrThrow()


        val sell = security != null && security.balance > 0u && yandexPrice > lastBuyPrice

        if (sell) {
            account.postSellOrder(figi, 1u, MarketPrice).getOrThrow()

            continue
        }

        if (balanceRub < yandexPrice) {
            break
        }

        val response = account.postBuyOrder(figi, 1u, MarketPrice).getOrThrow()

        if (response.status == OrderStatus.FILL) {
            lastBuyPrice = response.totalCost.quotation
        }

        delay(1000)
    }
}