package backend.db.bots

import backend.db.common.getDatabaseConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.sql.Timestamp

class BotsDatabase {
    val dbConfig = getDatabaseConfig().database

    val db = Database.connect("jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/algotrading", driver = "org.postgresql.Driver", user = dbConfig.user, password = dbConfig.pass)

    fun createStrategy(name: String, desctiption: String): Int? {
        return transaction {
            exec("select create_strategy('$name', '$desctiption');") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun createBot(name: String, strategyId: Int): Int? {
        return transaction {
            exec("select create_bot('$name', $strategyId);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun setIntParameter(botId: Int, paramId: Int, newValue: Int) {
        return transaction {
            exec("select set_int_parameter($botId, $paramId, $newValue);")
        }
    }

    fun setDoubleParameter(botId: Int, paramId: Int, newValue: Double) {
        return transaction {
            exec("select set_double_parameter($botId, $paramId, $newValue);")
        }
    }

    fun setStringParameter(botId: Int, paramId: Int, newValue: String) {
        return transaction {
            exec("select set_string_parameter($botId, $paramId, '$newValue');")
        }
    }

    fun getBotStrategy(botId: Int): Int? {
        return transaction {
            exec("select get_bot_strategy($botId);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun getIntParameter(botId: Int, paramId: Int): Int? {
        return transaction {
            exec("select get_int_parameter($botId, $paramId);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun getDoubleParameter(botId: Int, paramId: Int): Double? {
        return transaction {
            exec("select get_double_parameter($botId, $paramId);") {
                it.next()
                it.getDouble(1)
            }
        }
    }

    fun getStringParameter(botId: Int, paramId: Int): String? {
        return transaction {
            exec("select get_string_parameter($botId, $paramId);") {
                it.next()
                it.getString(1)
            }
        }
    }

    fun addOperation(
        botId: Int,
        operationId: Int,
        currentBotBalance: Double,
        stockId: Int,
        stockCount: Int,
        stockCost: Double,
        operationTime: Timestamp
    ) {
        return transaction {
            exec("select add_operation($botId, $operationId, $currentBotBalance, $stockId, $stockCount, $stockCost, '$operationTime');")
        }
    }

    // TODO: Parse table
    fun getOperations(bot_id: Int): List<OperationInfo> {
        val ops = transaction {
            exec("select * from get_operations($bot_id);") {
                it.next()
                val ops = mutableListOf<OperationInfo>()
                while (!it.isAfterLast) {
                    ops.add(
                        OperationInfo(
                            it.getInt(1),
                            it.getDouble(2),
                            it.getInt(3),
                            it.getInt(4),
                            it.getDouble(5),
                            it.getTimestamp(6)
                        )
                    )
                    it.next()
                }
                ops
            }
        } ?: listOf()

        return ops
    }

    fun getBotsByStrategy(strategyId: Int): List<Int> {
        val bots = transaction {
            exec("select * from get_bots_by_strategy($strategyId);") {
                val bots = mutableListOf<Int>()
                if (it.next()) {
                    while (!it.isAfterLast) {
                        bots.add(it.getInt(1))
                        it.next()
                    }
                }
                bots
            }
        } ?: listOf()

        return bots
    }

    fun getAllBots(): List<Int> {
        val bots = transaction {
            exec("select * from get_all_bots();") {
                it.next()
                val bots = mutableListOf<Int>()
                while (!it.isAfterLast) {
                    bots.add(it.getInt(1))
                    it.next()
                }
                bots
            }
        } ?: listOf()

        return bots
    }

    fun getBotName(botId: Int): String? {
        return transaction {
            exec("select * from get_bot_name($botId);") {
                it.next()
                it.getString(1)
            }
        }
    }

    data class OperationInfo(
        val operationId: Int, val botBalance: Double, val stockId: Int, val count: Int, val price: Double,
        val operationTime: Timestamp
    )

    companion object {
        const val SELL_OPERATION_ID = 1
        const val BUY_OPERATION_ID = 0
    }
}