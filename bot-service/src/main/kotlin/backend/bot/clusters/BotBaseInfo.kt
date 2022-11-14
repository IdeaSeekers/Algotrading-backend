package backend.bot.clusters

import backend.common.model.BotInfo

data class BotBaseInfo(
    val name: String,
    val strategyId: Int,
    val securityFigi: String,
    val parameters: List<BotInfo.Parameter>,
)