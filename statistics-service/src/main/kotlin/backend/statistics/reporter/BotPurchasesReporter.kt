package backend.statistics.reporter

import backend.statistics.StatisticsReporter
import backend.statistics.model.ActionInfo
import backend.statistics.model.ReportType
import backend.statistics.storage.MockStorage

class BotPurchasesReporter: Reporter {

    override fun report(botId: Int, actionInfo: ActionInfo) {
        StatisticsReporter.report(ReportType.BUY, botId, actionInfo)
    }
}