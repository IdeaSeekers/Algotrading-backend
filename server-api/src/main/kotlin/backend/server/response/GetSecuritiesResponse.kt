package backend.server.response

import backend.common.model.SecurityInfo
import kotlinx.serialization.Serializable

@Serializable
data class GetSecuritiesResponse(
    val securities: List<Security>
) {

    @Serializable
    data class Security(
        val id: Int,
        val name: String
    )

    companion object {
        fun fromSecurityInfo(listSecurityInfo: List<SecurityInfo>): GetSecuritiesResponse =
            GetSecuritiesResponse(
                listSecurityInfo.map {
                    Security(it.id, it.name)
                }
            )
    }
}
