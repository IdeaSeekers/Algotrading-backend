package backend.bot.clusters

import backend.common.model.BotInfo

data class BotBaseInfo(
    val name: String,
    val strategyId: Int,
    val parameters: List<BotInfo.Parameter>,
)