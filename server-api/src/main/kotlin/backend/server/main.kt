package backend.server

import backend.bot.clusters.SimpleCluster
import backend.bot.service.SimpleBotService
import backend.strategy.Parameters
import backend.strategy.ParametersDescription
import backend.strategy.Strategy
import backend.strategy.service.SimpleStrategyService
import backend.strategy.strategies.stupid.StupidStrategyContainerFactory
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val yandexStrategy = Strategy(
    1,
    "Yandex securities",
    "Stupid strategy",
    ParametersDescription("No parameters")
)

val strategyService = SimpleStrategyService {
    registerStrategy(yandexStrategy, StupidStrategyContainerFactory())
}

val token = "t.xIDSoPnEzIgS5gzNDXwIGkYg6lmDQ8IGkwZHAltdFICkHWeXAyqTGMYNGnBc1YhVMt-zyQfK7a1KBNGYwM_fjQ"

val tinkoffAccount = TinkoffSandboxService(token).run service@{
    val id = createSandboxAccount().getOrThrow()
    sandboxPayIn(id, 1000000.toUInt())
    TinkoffActualAccount(token, id)
}

val service = SimpleBotService {
    withStrategyService(strategyService)
    withAccount(tinkoffAccount)
    val cluster = SimpleCluster()
    addCluster(1, cluster)
}

fun mainSeregi(product: Product) {
    try {
        service.startBot(1, product.name, Parameters(product.name, product.rubles.toUInt())).getOrThrow()
    } catch (e: Throwable) {
        e.printStackTrace()
        println("The end ${product.name}!")
    }
}

@Serializable
data class Product(val name: String, val rubles: Int)

@Serializable
data class BotInfo(val name: String, val inputAmount: Int)

fun Application.productRoutes() {
    routing {
        post("/product") {
            val product = call.receive<Product>()
            mainSeregi(product)
            call.respondText("Product stored correctly", status = HttpStatusCode.Created)
        }
        get("/bots") {
            val bots = service
                .activeBots()
                .mapNotNull {
                    val bot = service.getBot(it).getOrNull() ?: return@mapNotNull null
                    BotInfo(
                        bot.name,
                        bot.parameters.rubles.toInt()
                    )
                }
            call.respond(HttpStatusCode.OK, bots)
        }
    }
}

fun Application.module() {
    install(CORS) {
        anyHost()
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        allowHeaders { true }
        allowNonSimpleContentTypes = true
        allowCredentials = true
        allowSameOrigin = true
    }
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    install(Routing)
    productRoutes()
}

fun main() {
    embeddedServer(
        Netty, port = 8080,
        host = "127.0.0.1"
    ) {
        module()
    }.start(wait = true)
}