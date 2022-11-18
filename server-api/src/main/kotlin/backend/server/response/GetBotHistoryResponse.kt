package backend.server.response

import backend.common.model.BotOperation
import kotlinx.serialization.Serializable

@Serializable
data class GetBotHistoryResponse(
    val operations: List<Operation>
) {

    @Serializable
    data class Operation(
        val type: Type,
        val timestamp: String,
        val executed_price: Double,
        val `return`: Double
    )

    @Serializable
    enum class Type {
        buy,
        sell,
    }

    companion object {
        fun fromListOperation(operations: List<BotOperation>) =
            GetBotHistoryResponse(
                operations.map { operation ->
                    val operationType = when (operation.type) {
                        BotOperation.Type.BUY -> Type.buy
                        BotOperation.Type.SELL -> Type.sell
                    }
                    Operation(
                        operationType,
                        operation.timestamp.toString(),
                        operation.executedPrice,
                        operation.returnValue
                    )
                }
            )
    }
}
