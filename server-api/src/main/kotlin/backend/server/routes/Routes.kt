package backend.server

import backend.server.routes.*
import io.ktor.application.*
import io.ktor.routing.*

fun Application.routes() {
    routing {
        route("/strategy") {
            getStrategies()
            getStrategy()
            getActiveBots()
            getAverageReturn()
            getReturnHistory()
        }
        route("/parameter") {
            getParameter()
        }
        route("/bot") {
            getBots()
            getBot()
            postBot()
            deleteBot()
            putBot()
            getReturn()
            getOperations()
        }
        route("security") {
            getSecurities()
        }
    }
}
