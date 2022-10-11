package backend.tinkoff.account

import backend.tinkoff.error.CannotOpenVirtualAccountError
import backend.tinkoff.error.waitForSuccess
import backend.tinkoff.model.*
import backend.tinkoff.response.PositionsResponse

class TinkoffVirtualAccountFactory(
    private val actualAccount: TinkoffActualAccount,
) {

    fun openVirtualAccount(withAvailablePositions: List<Position>): Result<TinkoffVirtualAccount> =
        updateAvailablePositions(withAvailablePositions).map {
            val virtualAccount = TinkoffVirtualAccount(actualAccount, withAvailablePositions)
            return Result.success(virtualAccount)
        }

    fun closeVirtualAccount(virtualAccount: TinkoffVirtualAccount) {
        TODO()
    }

    // internal

    private val initialAvailablePositions: PositionsResponse = waitForSuccess {
        actualAccount.getPositions()
    }

    private val availableCurrencies: MutableMap<IsoCode, Currency> =
        initialAvailablePositions.currencies
            .groupBy { it.isoCode }
            .mapValues { (_, currencies) ->
                currencies.reduce { acc, value -> (acc + value)!! }
            }
            .toMutableMap()

    private val availableSecurities: MutableMap<Figi, Security> =
        initialAvailablePositions.securities
            .groupBy { it.figi }
            .mapValues { (_, securities) ->
                securities.reduce { acc, security -> (acc + security)!! }
            }
            .toMutableMap()

    private fun updateAvailablePositions(requestPositions: List<Position>): Result<Unit> {
        val leftAvailablePositions = requestPositions.mapNotNull { position ->
            when (position) {
                is Currency -> leftAvailableCurrency(position)
                is Security -> leftAvailableSecurity(position)
            }
        }
        if (requestPositions.size != leftAvailablePositions.size) // contains nulls
            return Result.failure(CannotOpenVirtualAccountError("Cannot provide all requested positions"))

        leftAvailablePositions.forEach { leftAvailablePosition ->
            when (leftAvailablePosition) {
                is Currency ->
                    availableCurrencies[leftAvailablePosition.isoCode] = leftAvailablePosition
                is Security ->
                    availableSecurities[leftAvailablePosition.figi] = leftAvailablePosition
            }
        }

        return Result.success(Unit)
    }

    private fun leftAvailableCurrency(requestCurrency: Currency): Currency? {
        val availableCurrency = availableCurrencies[requestCurrency.isoCode]
            ?: return null // no such currency
        return availableCurrency - requestCurrency // == null if request > available
    }

    private fun leftAvailableSecurity(requestSecurity: Security): Security? {
        val availableSecurity = availableSecurities[requestSecurity.figi]
            ?: return null // no such security
        return availableSecurity - requestSecurity  // == null if request > available
    }
}