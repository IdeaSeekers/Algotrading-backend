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
        val availableSecurity = availableSecurities[requestedSecurity.figi]
            ?: return false
        return requestedSecurity.balance <= availableSecurity.balance
    }

    fun increase(by: Security) =
        updateWith(by, Security::plus)

    fun decrease(by: Security) =
        updateWith(by, Security::minus)

    fun updateWith(security: Security, mapping: (Security, Security) -> Security?) {
        val oldValue = availableSecurities[security.figi]
        val newValue = (if (oldValue == null) security else mapping(oldValue, security))
            ?: error("Cannot SecurityStorage update with security = $security")
        availableSecurities[security.figi] = newValue
    }

    fun mergeWith(securities: List<Security>) {
        securities.forEach { security ->
            availableSecurities.merge(security.figi, security, Security::plus)
        }
    }

    // internal

    private val availableSecurities: MutableMap<Figi, Security> =
        initialSecurities.toMutableMap()
}