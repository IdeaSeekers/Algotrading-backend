package backend.tinkoff.account

import backend.tinkoff.error.wrapTinkoffRequest
import backend.tinkoff.model.AccountId
import backend.tinkoff.model.Currency
import backend.tinkoff.model.Quotation
import ru.tinkoff.piapi.core.InvestApi

class TinkoffSandboxService(token: String) {

    fun createSandboxAccount(): Result<AccountId> =
        wrapTinkoffRequest {
            sandboxService.openAccount().get()
        }

    fun closeSandboxAccount(accountId: AccountId): Result<Unit> =
        wrapTinkoffRequest {
            sandboxService.closeAccount(accountId).get()
        }

    fun payIn(accountId: AccountId, rubles: UInt): Result<Unit> =
        wrapTinkoffRequest {
            val moneyValue = Currency("RUB", Quotation(rubles, 0U)).toMoneyValue()
            sandboxService.payIn(accountId, moneyValue).get()
        }

    // internal

    private val investApi = InvestApi.createSandbox(token)

    private val sandboxService = investApi.sandboxService
}