package backend.server.request

import kotlinx.serialization.Serializable

@Serializable
data class PutBotRequest(
    val status: Status,
) {
    @Serializable
    enum class Status {
        running,
        paused,
    }
}
