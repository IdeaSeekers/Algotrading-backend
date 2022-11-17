package backend.db.bots

import backend.db.common.getDatabaseConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.sql.Timestamp

class BotsDatabase {
    val dbConfig = getDatabaseConfig().database

    val db = Database.connect("jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/Algotrading", driver = "org.postgresql.Driver", user = dbConfig.user, password = dbConfig.pass)

    fun createStrategy(name: String, desctiption: String): Int? {
        return transaction {
            exec("select create_strategy('$name', '$desctiption');") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun createBot(name: String, desctiption: String, strategyId: Int): Int? {
        return transaction {
            exec("select create_bot('$name', '$desctiption', $strategyId);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun createIntParameter(param_name: String, param_description: String, default_param_value: Int): Int? {
        return transaction {
            exec("select create_int_parameter('$param_name', '$param_description', $default_param_value);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun createDoubleParameter(paramName: String, paramDescription: String, defaultParamValue: Double) {
        return transaction {
            exec("select create_double_parameter('$paramName', '$paramDescription', $defaultParamValue);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun addIntParameter(paramId: Int, stratId: Int): Int? {
        return transaction {
            exec("select add_int_parameter($paramId, $stratId);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun addDoubleParameter(paramId: Int, stratId: Int): Int? {
        return transaction {
            exec("select add_double_parameter($paramId, $stratId);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun setIntParameter(paramId: Int, botId: Int, newValue: Int) {
        return transaction {
            exec("select set_int_parameter($paramId, $botId, $newValue);")
        }
    }

    fun setDoubleParameter(paramId: Int, botId: Int, newValue: Double) {
        return transaction {
            exec("select set_double_parameter($paramId, $botId, $newValue);")
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

    fun getIntParameter(paramId: Int, botId: Int): Int? {
        return transaction {
            exec("select get_int_parameter($paramId, $botId);") {
                it.next()
                it.getInt(1)
            }
        }
    }

    fun getDoubleParameter(paramId: Int, botId: Int): Double? {
        return transaction {
            exec("select get_double_parameter($paramId, $botId);") {
                it.next()
                it.getDouble(1)
            }
        }
    }

    fun addOperation(
        botId: Int,
        operationId: Int,
        stockId: Int,
        stockCount: Int,
        stockCost: Double,
        operationTime: Timestamp
    ) {
        return transaction {
            exec("select add_operation($botId, $operationId, $stockId, $stockCount, $stockCost, '$operationTime');")
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
                            it.getInt(2),
                            it.getInt(3),
                            it.getDouble(4),
                            it.getTimestamp(5)
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

    data class OperationInfo(
        val operationId: Int, val stockId: Int, val count: Int, val price: Double,
        val operationTime: Timestamp
    )

    companion object {

        const val SELL_OPERATION_ID = 1
        const val BUY_OPERATION_ID = 0
    }
}