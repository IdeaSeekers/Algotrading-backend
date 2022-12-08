package backend.strategy.strategies.simple

import backend.strategy.Configuration
import backend.tinkoff.model.MarketPrice
import backend.tinkoff.model.OrderStatus
import backend.tinkoff.model.Quotation
import java.util.TreeSet
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

suspend fun simpleStrategy(config: Configuration) {
    val account = config.tinkoffAccount
    val figi = config.figi

    val boughtPrices = TreeSet<Quotation>()

    while (currentCoroutineContext().isActive) {

        val (currencies, securities) = account.getPositions().getOrThrow()

        val balanceRub = currencies.firstOrNull { it.isoCode == "rub" }?.quotation ?: Quotation.zero()
        val security = securities.firstOrNull { it.figi == figi }

        val figiPrice = account.getLastPrice(figi).getOrThrow()
        val figiLot = account.getLotByShare(figi).getOrThrow().toUInt()
        val figiLotPrice = figiPrice * figiLot

        val sell =
            security != null && security.balance > 0u && (!boughtPrices.isEmpty() && figiLotPrice > boughtPrices.first())

        if (sell) {
            account.postSellOrder(figi, 1u, MarketPrice).getOrThrow()
            boughtPrices.pollFirst()

            continue
        }

        if (balanceRub < figiLotPrice) {
            break
        }

        val response = account.postBuyOrder(figi, 1u, MarketPrice).getOrThrow()

        if (response.status == OrderStatus.FILL) {
            boughtPrices += response.totalCost.quotation
        }

        delay(5000)
    }
}