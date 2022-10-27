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
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun mainSeregi(product: Product) {
    val yandexStrategy = Strategy(
        1,
        "Yandex securities",
        "Stupid strategy",
        ParametersDescription("No parameters")
    )

    val strategyService = SimpleStrategyService {
        registerStrategy(yandexStrategy, StupidStrategyContainerFactory())
    }

    val token = ""

    val tinkoffAccount = TinkoffSandboxService(token).run service@{
        val id = createSandboxAccount().getOrThrow()
        sandboxPayIn(id, product.rubles.toUInt())
        TinkoffActualAccount(token, id)
    }

    val service = SimpleBotService {
        withStrategyService(strategyService)
        withAccount(tinkoffAccount)
        val cluster = SimpleCluster()
        addCluster(1, cluster)
    }
    service.startBot(1, product.name, Parameters(product.name, product.rubles.toUInt()))
}

@Serializable
data class Product(val name: String, val rubles: Int)

fun Application.productRoutes() {
    routing {
        post("/product") {
            val product = call.receive<Product>()
            try {
                mainSeregi(product)
            } catch (e: Throwable) {
                println("The End!")
            }
            call.respondText("Product stored correctly", status = HttpStatusCode.Created)
        }
    }
}

fun Application.module() {
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