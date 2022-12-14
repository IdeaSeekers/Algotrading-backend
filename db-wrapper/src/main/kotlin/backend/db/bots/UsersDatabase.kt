package backend.db.bots

import org.jetbrains.exposed.sql.transactions.transaction

class UsersDatabase : AlgotradingDatabase {
    init {
        initializeDatabase()
    }

    fun createUser(username: String, password: String, tinkoffToken: String) {
        transaction {
            exec("select create_user('$username', '$password', '$tinkoffToken');")
        }
    }

    fun getUser(username: String): DatabaseUser? {
        return transaction {
            exec("select * from get_user('$username');") {
                it.next()
                DatabaseUser(
                    it.getString(1),
                    it.getString(2),
                    it.getString(3),
                )
            }
        }
    }

    fun setNewToken(username: String, newTinkoffToken: String) {
        return transaction {
            exec("select set_user_tinkoff_token('$username', '$newTinkoffToken');")
        }
    }


    data class DatabaseUser(val username: String, val password: String, val tinkoffToken: String)
}