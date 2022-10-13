package backend.tinkoff.storage

import backend.tinkoff.model.Currency
import backend.tinkoff.model.IsoCode
import backend.tinkoff.model.Quotation

class CurrencyStorage(initialCurrencies: Map<IsoCode, Currency>) {

    companion object {
        fun fromList(initialCurrenciesList: List<Currency>) = CurrencyStorage(
            initialCurrenciesList
                .groupBy { it.isoCode }
                .mapValues { (_, currencies) ->
                    currencies.reduce { acc, value -> (acc + value)!! }
                }
        )
    }

    fun get(isoCode: IsoCode): Currency? =
        availableCurrencies[isoCode]

    fun getAll(): List<Currency> =
        availableCurrencies.values.toList()

    fun hasEnough(requestedCurrency: Currency): Boolean {
        if (requestedCurrency.quotation == Quotation.zero())
            return true
        val availableCurrency = availableCurrencies[requestedCurrency.isoCode]
            ?: return false
        return requestedCurrency.quotation <= availableCurrency.quotation
    }

    fun increase(by: Currency): Boolean =
        updateWith(by, Currency::plus)

    fun decrease(by: Currency): Boolean =
        updateWith(by, Currency::minus)

    fun forceIncrease(by: Currency) {
        if (!increase(by)) {
            error("Cannot increase CurrencyStorage by currency: $by")
        }
    }

    fun forceDecrease(by: Currency) {
        if (!decrease(by)) {
            error("Cannot decrease CurrencyStorage by currency: $by")
        }
    }

    fun updateWith(currency: Currency, mapping: (Currency, Currency) -> Currency?): Boolean {
        val oldValue = availableCurrencies[currency.isoCode]
        val newValue = (if (oldValue == null) currency else mapping(oldValue, currency))
            ?: return false
        availableCurrencies[currency.isoCode] = newValue
        return true
    }

    // internal

    private val availableCurrencies: MutableMap<IsoCode, Currency> =
        initialCurrencies.toMutableMap()
}