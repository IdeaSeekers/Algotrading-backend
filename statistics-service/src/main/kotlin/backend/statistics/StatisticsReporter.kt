package backend.statistics

import backend.statistics.model.ActionInfo
import backend.statistics.model.ReportType
import backend.statistics.storage.MockStorage.purchasesPerBot
import backend.statistics.storage.MockStorage.salesPerBot

object StatisticsReporter {

    @Synchronized
    fun report(reportType: ReportType, botId: Int, actionInfo: ActionInfo) {
        val statisticsStorage = when(reportType) {
            ReportType.BUY -> purchasesPerBot
            ReportType.SELL -> salesPerBot
        }
        if (statisticsStorage.containsKey(botId).not()) {
            statisticsStorage[botId] = mutableListOf()
        }
        statisticsStorage[botId]?.add(actionInfo)
    }
}
