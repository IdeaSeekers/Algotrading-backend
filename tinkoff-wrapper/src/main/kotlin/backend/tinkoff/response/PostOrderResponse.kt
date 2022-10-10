package backend.tinkoff.response

import backend.tinkoff.model.Figi
import backend.tinkoff.model.OrderId
import ru.tinkoff.piapi.contract.v1.PostOrderResponse as TinkoffPostOrderResponse

data class PostOrderResponse(
    val figi: Figi,
    val orderId: OrderId,
) {
    companion object {
        fun fromTinkoff(tinkoffResponse: TinkoffPostOrderResponse): PostOrderResponse =
            PostOrderResponse(
                tinkoffResponse.figi,
                tinkoffResponse.orderId,
            )
    }
}
