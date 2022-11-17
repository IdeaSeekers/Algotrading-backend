package backend.server.util

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*

const val defaultErrorMessage = "Something went wrong"

suspend fun ApplicationCall.exception(exception: Throwable) =
    respondText(
        text = exception.message ?: defaultErrorMessage,
        status = HttpStatusCode.InternalServerError
    )

suspend fun ApplicationCall.badRequest(message: String?) =
    respondText(
        text = message ?: defaultErrorMessage,
        status = HttpStatusCode.BadRequest
    )
