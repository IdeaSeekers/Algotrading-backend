package backend.tinkoff.storage

import backend.tinkoff.model.Figi
import backend.tinkoff.model.Security

class SecurityStorage(initialSecurities: Map<Figi, Security>) {

    companion object {
        fun fromList(initialSecuritiesList: List<Security>) = SecurityStorage(
            initialSecuritiesList
                .groupBy { it.figi }
                .mapValues { (_, securities) ->
                    securities.reduce { acc, security -> (acc + security)!! }
                }
        )
    }

    fun get(figi: Figi): Security? =
        availableSecurities[figi]

    fun getAll(): List<Security> =
        availableSecurities.values.toList()

    fun hasEnough(requestedSecurity: Security): Boolean {
        if (requestedSecurity.balance == 0U)
            return true
        val availableSecurity = availableSecurities[requestedSecurity.figi]
            ?: return false
        return requestedSecurity.balance <= availableSecurity.balance
    }

    fun increase(by: Security): Boolean =
        updateWith(by, Security::plus)

    fun decrease(by: Security): Boolean =
        updateWith(by, Security::minus)

    fun forceIncrease(by: Security) {
        if (!increase(by)) {
            error("Cannot increase SecurityStorage by security: $by")
        }
    }

    fun forceDecrease(by: Security) {
        if (!decrease(by)) {
            error("Cannot decrease SecurityStorage by security: $by")
        }
    }

    fun updateWith(security: Security, mapping: (Security, Security) -> Security?): Boolean {
        val oldValue = availableSecurities[security.figi]
        val newValue = (if (oldValue == null) security else mapping(oldValue, security))
            ?: return false
        availableSecurities[security.figi] = newValue
        return true
    }

    // internal

    private val availableSecurities: MutableMap<Figi, Security> =
        initialSecurities.toMutableMap()
}