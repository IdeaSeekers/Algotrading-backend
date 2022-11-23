package backend.server.response

import backend.common.model.ReturnInfo
import kotlinx.serialization.Serializable
import java.time.ZoneOffset

@Serializable
class GetStrategyReturnHistoryResponse(
    val return_history: List<Info>
) {

    @Serializable
    data class Info(
        val timestamp: Long,
        val average_return: Double
    )

    companion object {
        fun fromListReturnInfo(listInfo: List<ReturnInfo>) =
            GetStrategyReturnHistoryResponse(
                listInfo.map { info ->
                    Info(info.timestamp.toEpochSecond(ZoneOffset.UTC), info.returnValue)
                }
            )
    }
}
