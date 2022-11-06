package backend.common.model

data class BotInfo(
    val name: String,
    val strategyId: Int,
    val balance: Double, // in rubles
    val securityFigi: String,
    val status: Status,
    val parameters: List<Parameter>,
) {
    enum class Status {
        RUNNING,
        PAUSED,
        STOPPED,
        UNKNOWN,
    }

    data class Parameter(
        val id: Int,
        val value: Double,
    )
}
