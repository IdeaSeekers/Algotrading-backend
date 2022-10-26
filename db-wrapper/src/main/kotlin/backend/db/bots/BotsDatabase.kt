package backend.db.bots

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.sql.Timestamp

class BotsDatabase {
    val url = "jdbc:postgresql://localhost:5432/algotrading"
    val user = "postgres"
    val passwd = "jaja"

    val db = Database.connect(url, driver = "org.postgresql.Driver", user = user, password = passwd)

    fun create_strategy(name: String, desctiption: String): Int? {
        return transaction {
            exec("select create_strategy('$name', '$desctiption');") { it.next(); it.getInt(1) }
        }
    }

    fun create_bot(name: String, desctiption: String, strategyId: Int): Int? {
        return transaction {
            exec("select create_bot('$name', '$desctiption', $strategyId);") { it.next(); it.getInt(1) }
        }
    }

    fun create_int_parameter(param_name: String, param_description: String, default_param_value: Int): Int? {
        return transaction {
            exec("select create_int_parameter('$param_name', '$param_description', $default_param_value);") { it.next(); it.getInt(1) }
        }
    }

    fun create_double_parameter(param_name: String, param_description: String, default_param_value: Double) {
        return transaction {
            exec("select create_double_parameter('$param_name', '$param_description', $default_param_value);") { it.next(); it.getInt(1) }
        }
    }

    fun add_int_parameter(paramId: Int, stratId: Int): Int? {
        return transaction {
            exec("select add_int_parameter($paramId, $stratId);") { it.next(); it.getInt(1) }
        }
    }

    fun add_double_parameter(paramId: Int, stratId: Int): Int? {
        return transaction {
            exec("select add_double_parameter($paramId, $stratId);") { it.next(); it.getInt(1) }
        }
    }

    fun set_int_parameter(paramId: Int, botId: Int, newValue: Int) {
        return transaction {
            exec("select set_int_parameter($paramId, $botId, $newValue);")
        }
    }

    fun set_double_parameter(paramId: Int, botId: Int, newValue: Double) {
        return transaction {
            exec("select set_double_parameter($paramId, $botId, $newValue);")
        }
    }

    fun get_bot_strategy(botId: Int): Int? {
        return transaction {
            exec("select get_bot_strategy($botId);") { it.next(); it.getInt(1) }
        }
    }

    fun get_int_parameter(paramId: Int, botId: Int): Int? {
        return transaction {
            exec("select get_int_parameter($paramId, $botId);") { it.next(); it.getInt(1) }
        }
    }

    fun get_double_parameter(paramId: Int, botId: Int): Double? {
        return transaction {
            exec("select get_double_parameter($paramId, $botId);") { it.next(); it.getDouble(1) }
        }
    }

    fun add_operation(bot_id: Int, op_id: Int, stock_id: Int, stock_count: Int, stock_cost: Double, op_time: Timestamp) {
        return transaction {
            exec("select add_operation($bot_id, $op_id, $stock_id, $stock_count, $stock_cost, '$op_time');")
        }
    }

    // TODO: Parse table
    fun get_operations(bot_id: Int) {
        return transaction {
            exec("select get_operations($bot_id);")
        }
    }

}