package backend.tinkoff.account

class TinkoffVirtualAccountFactory(
    private val actualAccount: TinkoffActualAccount,
) {

    fun createVirtualAccount(): TinkoffVirtualAccount =
        TinkoffVirtualAccount(actualAccount)
}