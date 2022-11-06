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

class SimpleCluster : BotCluster {
    private val activeBots: MutableMap<BotUid, Bot> = mutableMapOf()
    private val containers: MutableMap<BotUid, StrategyContainer> = mutableMapOf()

    override fun activeBots(): List<BotUid> =
        activeBots.keys.toList()

    override fun getBot(uid: BotUid): Result<Bot> =
        activeBots[uid]?.let { Result.success(it) } ?: Result.failure(BotNotFoundException(uid))

    override fun deploy(container: StrategyContainer, uid: BotUid, name: BotName, parameters: Parameters, tinkoffAccount: TinkoffAccount): Result<Unit> {
        val configuration = Configuration(
            parameters,
            tinkoffAccount
        )
        val res = container.start(configuration)

        return if (res) {
            val bot = Bot(
                uid,
                name,
                parameters,
                Status.RUNNING
            )

            activeBots[uid] = bot
            containers[uid] = container

            Result.success(Unit)
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
}