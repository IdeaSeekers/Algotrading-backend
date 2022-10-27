package backend.statistics

import backend.statistics.model.ActionInfo
import backend.statistics.model.Price
import backend.statistics.model.ReportType
import backend.statistics.storage.MockStorage

class StatisticsAggregator {

    fun getActionsByBotId(botId: Int): MutableList<ActionInfo> {
        val botSales = getActionsByBotId(ReportType.SELL, botId)
        val botPurchases = getActionsByBotId(ReportType.BUY, botId)

        val allBotActions = mutableListOf<ActionInfo>()
        allBotActions.addAll(botSales)
        allBotActions.addAll(botPurchases)
        // TODO: sort by timestamp
        return allBotActions
    }

    fun getActionsByBotId(report: ReportType, botId: Int): MutableList<ActionInfo> {
        return when (report) {
            ReportType.BUY -> MockStorage.purchasesPerBot.getOrDefault(botId, mutableListOf())
            ReportType.SELL -> MockStorage.salesPerBot.getOrDefault(botId, mutableListOf())
        }
    }

    fun getSumPurchasesByBotId(botId: Int): Price? =
        MockStorage.purchasesPerBot[botId]?.map { it.price }?.reduce(Price::plus)


    fun getSumSalesByBotId(botId: Int): Price? =
        MockStorage.salesPerBot[botId]?.map { it.price }?.reduce(Price::plus)
}
