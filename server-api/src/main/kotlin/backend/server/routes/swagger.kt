@file:OptIn(KtorExperimentalLocationsAPI::class)

package backend.server.routes

import backend.server.request.PostBotRequest
import backend.server.request.UserSignInRequest
import backend.server.request.UserSignUpRequest
import backend.server.response.*
import de.nielsfalk.ktor.swagger.version.shared.Group
import io.ktor.locations.*

// strategy

@Group("strategy")
@Location("/strategy")
class SwaggerStrategies {
    companion object {
        val responseExample = GetStrategiesResponse(
            listOf(0, 1, 2).map { GetStrategiesResponse.Strategy(it) }
        )
    }
}

@Group("strategy")
@Location("/strategy/{id}")
class SwaggerStrategy(val id: Int) {
    companion object {
        val responseExample = GetStrategyResponse(
            "Strategy name",
            "Strategy description",
            GetStrategyResponse.Risk.high,
            listOf(0, 1).map { GetStrategyResponse.Parameter(it) }
        )
    }
}

@Group("strategy")
@Location("/strategy/{id}/active")
class SwaggerActiveBots(val id: Int) {
    companion object {
        val responseExample = GetActiveBotsCountResponse(
            GetActiveBotsCountResponse.BotsCount(2)
        )
    }
}

@Group("strategy")
@Location("/strategy/{id}/average_return")
class SwaggerAverageReturn(val id: Int) {
    companion object {
        val responseExample = GetStrategyAverageReturnResponse(
            14.15
        )
    }
}

@Group("strategy")
@Location("/strategy/{id}/return_history")
class SwaggerReturnHistory(val id: Int) {
    companion object {
        val responseExample = GetStrategyReturnHistoryResponse(
            listOf(
                GetStrategyReturnHistoryResponse.Info(94528736478, 10.6),
                GetStrategyReturnHistoryResponse.Info(94528736978, 166.98),
            )
        )
    }
}

// bot

@Group("bot")
@Location("/bot")
class SwaggerBots {
    companion object {
        val responseExampleGet = GetBotsResponse(
            listOf(0, 1, 2).map { GetBotsResponse.Bot(it) }
        )

        val requestExamplePost = PostBotRequest(
            "Bot name",
            PostBotRequest.Strategy(0),
            listOf(
                PostBotRequest.Parameter(0, 0.0),
                PostBotRequest.Parameter(1, 999.99),
            )
        )

        val responseExamplePost = PostBotResponse(
            PostBotResponse.Bot(0)
        )
    }
}

@Group("bot")
@Location("/bot/{id}")
class SwaggerBot(val id: Int) {
    companion object {
        val responseExampleGet = GetBotResponse(
            "Bot name",
            0,
            1000.00,
            GetBotResponse.Status.running,
            listOf(
                GetBotResponse.Parameter(0, 0.0),
                GetBotResponse.Parameter(1, 999.99)
            )
        )

        val responseExampleDelete = Unit

        val responseExamplePut = Unit
    }
}

@Group("bot")
@Location("/bot/{id}/return")
class SwaggerBotReturn(val id: Int) {
    companion object {
        val responseExample = GetBotReturnResponse(
            15.14
        )
    }
}

@Group("bot")
@Location("/bot/{id}/operations")
class SwaggerBotOperations(val id: Int) {
    companion object {
        val responseExample = GetBotHistoryResponse(
            listOf(
                GetBotHistoryResponse.Operation(GetBotHistoryResponse.Type.buy, "2022.11.27T14:22", 100.4, 0.0),
                GetBotHistoryResponse.Operation(GetBotHistoryResponse.Type.sell, "2022.11.27T14:49", 101.1, 0.7),
            )
        )
    }
}

// parameter

@Group("parameter")
@Location("/parameter/{id}")
class SwaggerParameter(val id: Int) {
    companion object {
        val responseExample = GetParameterResponse(
            "Parameter name",
            "Parameter description",
            GetParameterResponse.Type.int,
            0.0,
            100.0
        )
    }
}

// security

@Group("security")
@Location("/security")
class SwaggerSecurities {
    companion object {
        val responseExample = GetSecuritiesResponse(
            listOf(
                GetSecuritiesResponse.Security(0, "Нижнекамскнефтехим"),
                GetSecuritiesResponse.Security(1, "Яндекс"),
                GetSecuritiesResponse.Security(2, "Сбер"),
            )
        )
    }
}

// user

@Group("user")
@Location("/user/signup")
class SwaggerUserSignUp {
    companion object {
        val requestExample = UserSignUpRequest(
            "amogus",
            "sus",
            "<tinkoff-token>"
        )

        val responseExample = Unit
    }
}

@Group("user")
@Location("/user/signin")
class SwaggerUserSignIn {
    companion object {
        val requestExample = UserSignInRequest(
            "amogus",
            "sus",
        )

        val responseExample = UserSignInResponse(
            "<jwt-token>",
            "<tinkoff-token>"
        )
    }
}

@Group("user")
@Location("/user/amount")
class SwaggerUserAmount {
    companion object {
        val responseExample = UserAmountResponse(123456.78)
    }
}
