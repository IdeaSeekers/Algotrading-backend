package backend.statistics.storage

import backend.statistics.model.ActionInfo

object MockStorage {
    // NOTE: temporary solution. Change to calling dbWrapper when api will be available
    val salesPerBot = mutableMapOf<Int, MutableList<ActionInfo>>()
    val purchasesPerBot = mutableMapOf<Int, MutableList<ActionInfo>>()
}