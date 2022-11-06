package backend.server.routes

import io.ktor.routing.*

fun Route.getBots() {
    get("/") {
        //
    }
}

fun Route.getBot() {
    get("/{id}") {
        //
    }
}

fun Route.postBot() {
    post("/{id}") {
        //
    }
}

fun Route.deleteBot() {
    delete("/{id}") {
        //
    }
}

fun Route.putBot() {
    put("/{id}") {
        //
    }
}

fun Route.getReturn() {
    get("/{id}/return") {
        // call.request.queryParameters["timestamp_from"]
        // call.request.queryParameters["timestamp_to"]
        //
    }
}

fun Route.getOperations() {
    get("/{id}/operations") {
        //
    }
}
