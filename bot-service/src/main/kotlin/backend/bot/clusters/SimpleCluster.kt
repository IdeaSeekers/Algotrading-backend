package backend.bot.clusters

import backend.bot.BotNotFoundException
import backend.bot.ContainerStartException
import backend.bot.api.Bot
import backend.bot.api.BotCluster
import backend.bot.api.BotName
import backend.bot.api.BotUid
import backend.strategy.Configuration
import backend.strategy.Parameters
import backend.strategy.Status
import backend.strategy.StrategyContainer
import backend.tinkoff.account.TinkoffAccount
import java.util.concurrent.atomic.AtomicInteger

class SimpleCluster(
    configure: Configuration.() -> Unit
) : BotCluster {
    private val activeBots: MutableMap<BotUid, Bot> = mutableMapOf()
    private val containers: MutableMap<BotUid, StrategyContainer> = mutableMapOf()
    private val botNumberer: AtomicInteger

    init {
        val configuration = InternalConfiguration().apply(configure)
        botNumberer = configuration.botNumberer
    }

    override fun activeBots(): List<BotUid> =
        activeBots.keys.toList()

    override fun getBot(uid: BotUid): Result<Bot> =
        activeBots[uid]?.let { Result.success(it) } ?: Result.failure(BotNotFoundException(uid))

    override fun deploy(container: StrategyContainer, name: BotName, parameters: Parameters, tinkoffAccount: TinkoffAccount): Result<BotUid> {
        val configuration = Configuration(
            parameters,
            tinkoffAccount
        )
        val res = container.start(configuration)

        return if (res) {
            val uid = botNumberer.incrementAndGet()
            val bot = Bot(
                uid,
                name,
                parameters,
                Status.RUNNING
            )

            activeBots[uid] = bot
            containers[uid] = container

            Result.success(uid)
        } else {
            Result.failure(ContainerStartException())
        }
    }

    override fun stopBot(uid: BotUid): Boolean {
        val container = containers[uid] ?: return false // already deleted
        if (!container.stop()) {
            container.die()
        }
        activeBots.remove(uid)
        containers.remove(uid)
        return true
    }

    interface Configuration {
        fun withBotNumberer(numberer: AtomicInteger)
    }

    // internal
    private class InternalConfiguration : Configuration {
        lateinit var botNumberer: AtomicInteger

        override fun withBotNumberer(numberer: AtomicInteger) {
            botNumberer = numberer
        }
    }
}