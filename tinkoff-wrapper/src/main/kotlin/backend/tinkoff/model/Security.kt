package backend.tinkoff.model

data class Security(
    val figi: Figi,
    val balance: UInt,
) : Position {

    operator fun plus(other: Security): Security? {
        if (figi != other.figi)
            return null
        return this.copy(balance = balance + other.balance)
    }

    operator fun minus(other: Security): Security? {
        if (figi != other.figi)
            return null
        if (balance < other.balance)
            return null
        return this.copy(balance = balance - other.balance)
    }
}
