package backend.tinkoff.storage

import backend.tinkoff.model.Currency
import backend.tinkoff.model.IsoCode

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
        val availableCurrency = availableCurrencies[requestedCurrency.isoCode]
            ?: return false
        return requestedCurrency.quotation <= availableCurrency.quotation
    }

    fun increase(by: Currency) =
        updateWith(by, Currency::plus)

    fun decrease(by: Currency) =
        updateWith(by, Currency::minus)

    fun updateWith(currency: Currency, mapping: (Currency, Currency) -> Currency?) {
        val oldValue = availableCurrencies[currency.isoCode]
        val newValue = (if (oldValue == null) currency else mapping(oldValue, currency))
            ?: error("Cannot CurrencyStorage update with currency = $currency")
        availableCurrencies[currency.isoCode] = newValue
    }

    fun mergeWith(currencies: List<Currency>) {
        currencies.forEach { currency ->
            availableCurrencies.merge(currency.isoCode, currency, Currency::plus)
        }
    }

    // internal

    private val availableCurrencies: MutableMap<IsoCode, Currency> =
        initialCurrencies.toMutableMap()
}