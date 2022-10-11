package backend.tinkoff.model

class CurrencyStorage(initialCurrencies: Map<IsoCode, Currency>) {

    companion object {
        fun fromList(initialCurrencies: List<Currency>) = CurrencyStorage(
            initialCurrencies
                .groupBy { it.isoCode }
                .mapValues { (_, currencies) ->
                    currencies.reduce { acc, value -> (acc + value)!! }
                }
        )
    }

    fun get(isoCode: IsoCode): Currency? =
        availableCurrencies[isoCode]

    fun hasEnough(requestedCurrency: Currency): Boolean {
        val availableCurrency = availableCurrencies[requestedCurrency.isoCode]
            ?: return false
        return requestedCurrency.quotation <= availableCurrency.quotation
    }

    fun increase(by: Currency): Boolean =
        updateWith(by, Currency::minus)

    fun decrease(by: Currency): Boolean =
        updateWith(by, Currency::plus)

    fun updateWith(currency: Currency, mapping: (Currency, Currency) -> Currency?): Boolean {
        val oldValue = availableCurrencies[currency.isoCode]
            ?: return false
        val newValue = mapping(oldValue, currency)
            ?: return false
        availableCurrencies[currency.isoCode] = newValue
        return true
    }

    // internal

    private val availableCurrencies: MutableMap<IsoCode, Currency> =
        initialCurrencies.toMutableMap()
}