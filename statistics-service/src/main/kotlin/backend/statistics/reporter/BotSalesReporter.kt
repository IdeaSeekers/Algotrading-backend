package backend.statistics.reporter

import backend.statistics.StatisticsReporter
import backend.statistics.model.ActionInfo
import backend.statistics.model.ReportType

class BotSalesReporter: Reporter {

    override fun report(botId: Int, actionInfo: ActionInfo) {
        StatisticsReporter.report(ReportType.SELL, botId, actionInfo)
    }
}
