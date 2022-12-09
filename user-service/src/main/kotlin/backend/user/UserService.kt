package backend.user

import backend.bot.BotService
import backend.bot.clusters.SimpleCluster
import backend.bot.service.DbBotService
import backend.common.model.Id
import backend.common.model.User
import backend.db.bots.BotsDatabase
import backend.strategy.StrategyService
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService
import backend.tinkoff.account.TinkoffVirtualAccountFactory

open class UserService(
    private val botsDatabase: BotsDatabase,
    private val strategyService: StrategyService
) {

    fun addUser(user: User): Result<Unit> =
        if (users.containsKey(user.username))
            updateExistentUser(user)
        else
            addNewUser(user)

    fun findUser(username: String, password: String): User? {
        val maybeUser = users[username]
        return if (maybeUser?.password == password) maybeUser else null
    }

    fun getTinkoffAccount(username: Username): TinkoffActualAccount? =
        tinkoffAccounts[username]

    fun getBotService(username: Username): BotService? =
        botServices[username]

    fun getAllBotServices(): List<BotService> =
        botServices.values.toList()

    // internal

    private fun addNewUser(user: User): Result<Unit> {
        val tinkoffAccount = initTinkoffAccount(user).getOrElse {
            return Result.failure(it)
        }
        val botService = initBotService(tinkoffAccount)

        tinkoffAccounts[user.username] = tinkoffAccount
        botServices[user.username] = botService
        users[user.username] = user

        return Result.success(Unit)
    }

    private fun updateExistentUser(user: User): Result<Unit> {
        users[user.username] = user
        return Result.success(Unit)
    }

    // init methods

    protected open fun initTinkoffAccount(user: User): Result<TinkoffActualAccount> {
        val sandboxService = TinkoffSandboxService(user.tinkoff)
        val accountId = sandboxService.createSandboxAccount()
            .onFailure { return Result.failure(it) }
            .getOrThrow()
        sandboxService.sandboxPayIn(accountId, rubles = 1_000_000_u)
        val account = TinkoffActualAccount(user.tinkoff, accountId)
        return Result.success(account)
    }

    protected open fun initBotService(tinkoffAccount: TinkoffActualAccount): BotService =
        DbBotService(botsDatabase) {
            withStrategyService(strategyService)
            val tinkoffAccountFactory = TinkoffVirtualAccountFactory(tinkoffAccount)
            // TODO: use user.username in BotService for bot ids ?
            val cluster = SimpleCluster(
                Id.simpleStrategyUid,
                Id.balanceHyperParameterUid,
                Id.figiHyperParameterUid,
                tinkoffAccountFactory
            )
            addCluster(Id.simpleStrategyUid, cluster)
        }

    // internal fields

    private val users = mutableMapOf<Username, User>() // TODO: Database here

    private val tinkoffAccounts = mutableMapOf<Username, TinkoffActualAccount>()

    private val botServices = mutableMapOf<Username, BotService>()
}
