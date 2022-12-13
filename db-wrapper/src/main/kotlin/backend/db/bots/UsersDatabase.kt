package backend.db.bots

import org.jetbrains.exposed.sql.transactions.transaction

class UsersDatabase : AlgotradingDatabase {
    init {
        initializeDatabase()
    }

    fun createUser(username: String, password: String, tinkoffToken: String): Int? {
        return transaction {
            exec("select create_user('$username', '$password', '$tinkoffToken');") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun getUser(userId: Int): DatabaseUser? {
        return transaction {
            exec("select * from get_user($userId);") {
                it.next()
                DatabaseUser(
                    it.getString(1),
                    it.getString(2),
                    it.getString(3),
                )
            }
        }
    }

    fun setNewToken(userId: Int, newTinkoffToken: String) {
        return transaction {
            exec("select set_user_tinkoff_token($userId, '$newTinkoffToken');")
        }
    }


    data class DatabaseUser(val username: String, val password: String, val tinkoffToken: String)
}