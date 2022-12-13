package backend.db.bots

import backend.db.common.getDatabaseConfig
import org.jetbrains.exposed.sql.Database

interface AlgotradingDatabase {
    fun initializeDatabase() {
        val dbConfig = getDatabaseConfig().database
        Database.connect("jdbc:postgresql://${dbConfig.host}:${dbConfig.port}/algotrading", driver = "org.postgresql.Driver", user = dbConfig.user, password = dbConfig.pass)
    }
}