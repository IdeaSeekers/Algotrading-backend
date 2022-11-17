package backend.db.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.nio.file.Paths

data class DatabaseCredentials(val host: String = "localhost", val port: Int = 1234, val user: String = "postgres", val pass: String = "")
data class DatabaseConfig(val database: DatabaseCredentials = DatabaseCredentials())

fun getDatabaseConfig(): DatabaseConfig {
    val f = Paths.get("./db-wrapper/src/main/resources/conf.yaml").toFile()
    if (!f.exists()) {
        return DatabaseConfig()
    }

    val mapper = ObjectMapper(YAMLFactory())
    mapper.findAndRegisterModules()

    return mapper.readValue(f, DatabaseConfig::class.java)
}
