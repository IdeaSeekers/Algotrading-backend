package backend.server.response

import backend.common.model.HyperParameterInfo
import kotlinx.serialization.Serializable

@Serializable
data class GetParameterResponse(
    val name: String,
    val description: String,
    val type: Type,
    val min: Double? = null,
    val max: Double? = null,
) {
        @Serializable
        enum class Type {
            int,
            float,
            string,
        }

        companion object {
            fun fromHyperParameterInfo(hyperParameterInfo: HyperParameterInfo): GetParameterResponse {
                val type = when (hyperParameterInfo.type) {
                    HyperParameterInfo.Type.INT -> Type.int
                    HyperParameterInfo.Type.FLOAT -> Type.float
                    HyperParameterInfo.Type.STRING -> Type.string
                }
                return GetParameterResponse(
                    hyperParameterInfo.name,
                    hyperParameterInfo.description,
                    type,
                    hyperParameterInfo.min,
                    hyperParameterInfo.max,
                )
            }
        }
    }