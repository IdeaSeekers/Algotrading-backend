package backend.tinkoff

class TinkoffVirtualAccountFactory(
    private val actualAccount: TinkoffActualAccount,
) {

    fun createVirtualAccount(): TinkoffVirtualAccount =
        TinkoffVirtualAccount(actualAccount)
}