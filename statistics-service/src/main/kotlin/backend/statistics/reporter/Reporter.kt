package backend.statistics.reporter

import backend.statistics.model.ActionInfo

interface Reporter {

    fun report(botId: Int, actionInfo: ActionInfo)
}
