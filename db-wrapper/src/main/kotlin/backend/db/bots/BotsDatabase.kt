@file:Suppress("SqlNoDataSourceInspection")

package backend.db.bots

import backend.db.common.getDatabaseConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Timestamp

open class BotsDatabase : AlgotradingDatabase {
    init {
        initializeDatabase()
    }

    fun createBot(name: String, strategyId: Int, ownerUsername: String): Int? {
        return transaction {
            exec("select create_bot('$name', $strategyId, '$ownerUsername');") {
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
        return transaction {
            exec("select * from get_operations($bot_id);") {
                val operations = mutableListOf<OperationInfo>()
                while (it.next()) {
                    operations.add(
                        OperationInfo(
                            it.getInt(1),
                            it.getDouble(2),
                            it.getInt(3),
                            it.getInt(4),
                            it.getDouble(5),
                            it.getTimestamp(6)
                        )
                    )
                }
                operations
            }
        } ?: listOf()
    }

    fun getBotsByStrategy(strategyId: Int): List<Int> {
        return transaction {
            exec("select * from get_bots_by_strategy($strategyId);") {
                val bots = mutableListOf<Int>()
                while (it.next()) {
                    bots.add(it.getInt(1))
                }
                bots
            }
        } ?: listOf()
    }

    fun getAllBots(): List<Int> {
        return transaction {
            exec("select * from get_all_bots();") {
                val bots = mutableListOf<Int>()
                while (it.next()) {
                    bots.add(it.getInt(1))
                }
                bots
            }
        } ?: listOf()
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
        const val BUY_OPERATION_ID = 1
        const val SELL_OPERATION_ID = 2
    }
}