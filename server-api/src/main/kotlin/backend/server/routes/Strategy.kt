package backend.server.routes

import io.ktor.routing.*

fun Route.getStrategies() {
    get("/") {
        //
    }
}

fun Route.getStrategy() {
    get("/{id}") {
        //
    }
}

fun Route.getActiveBots() {
    get("/{id}/active") {
        //
    }
}

fun Route.getAverageReturn() {
    get("/{id}/average_return") {
        // call.request.queryParameters["timestamp_from"]
        // call.request.queryParameters["timestamp_to"]
        //
    }
}

fun Route.getReturnHistory() {
    get("/{id}/return_history") {
        //
    }
}
