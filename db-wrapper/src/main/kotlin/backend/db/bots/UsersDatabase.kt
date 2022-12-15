@file:Suppress("LeakingThis", "SqlNoDataSourceInspection")

package backend.db.bots

import org.jetbrains.exposed.sql.transactions.transaction

open class UsersDatabase : AlgotradingDatabase {
    init {
        initializeDatabase()
    }

    open fun createUser(username: String, password: String, tinkoffToken: String) {
        transaction {
            exec("select create_user('$username', '$password', '$tinkoffToken');")
        }
    }

    open fun getUser(username: String): DatabaseUser? {
        return transaction {
            val user = exec("select * from get_user('$username');") {
                if (!it.next())
                    return@exec DatabaseUser("null", "null", "null")
                DatabaseUser(
                    it.getString(1),
                    it.getString(2),
                    it.getString(3),
                )
            }
            if (user == DatabaseUser("null", "null", "null"))
                null
            else
                user
        }
    }

    fun setNewToken(username: String, newTinkoffToken: String) {
        return transaction {
            exec("select set_user_tinkoff_token('$username', '$newTinkoffToken');")
        }
    }


    data class DatabaseUser(val username: String, val password: String, val tinkoffToken: String)
}