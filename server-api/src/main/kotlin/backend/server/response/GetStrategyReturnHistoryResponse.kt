package backend.server.response

import backend.common.model.ReturnInfo
import kotlinx.serialization.Serializable

@Serializable
class GetStrategyReturnHistoryResponse(
    val return_history: List<Info>
) {

    @Serializable
    data class Info(
        val timestamp: String,
        val average_return: Double
    )

    companion object {
        fun fromListReturnInfo(listInfo: List<ReturnInfo>) =
            GetStrategyReturnHistoryResponse(
                listInfo.map { info ->
                    Info(info.timestamp.toString(), info.returnValue)
                }
            )
    }
}
