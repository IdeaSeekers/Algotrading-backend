package backend.common.model

data class StrategyInfo(
    val name: String,
    val description: String,
    val risk: Risk,
    val parameterIds: List<Int>,
) {
    enum class Risk {
        LOW,
        MEDIUM,
        HIGH,
    }
}
