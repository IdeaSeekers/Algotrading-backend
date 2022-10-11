package backend.tinkoff.response

import backend.tinkoff.model.Currency
import backend.tinkoff.model.Figi
import backend.tinkoff.model.OrderId
import ru.tinkoff.piapi.contract.v1.PostOrderResponse as TinkoffPostOrderResponse

data class PostOrderResponse(
    val figi: Figi,
    val orderId: OrderId,
    val totalCost: Currency,
) {
    companion object {
        fun fromTinkoff(tinkoffResponse: TinkoffPostOrderResponse): PostOrderResponse =
            PostOrderResponse(
                tinkoffResponse.figi,
                tinkoffResponse.orderId,
                Currency.fromMoneyValue(tinkoffResponse.totalOrderAmount),
            )
    }
}
