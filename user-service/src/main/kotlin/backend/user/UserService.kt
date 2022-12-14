package backend.user

import backend.bot.BotService
import backend.bot.clusters.SimpleCluster
import backend.bot.service.DbBotService
import backend.common.model.Id
import backend.common.model.User
import backend.db.bots.BotsDatabase
import backend.db.bots.UsersDatabase
import backend.strategy.StrategyService
import backend.tinkoff.account.TinkoffActualAccount
import backend.tinkoff.account.TinkoffSandboxService
import backend.tinkoff.account.TinkoffVirtualAccountFactory

open class UserService(
    private val usersDatabase: UsersDatabase,
    private val botsDatabase: BotsDatabase,
    private val strategyService: StrategyService
) {

    fun addUser(user: User): Result<Unit> =
        if (usersDatabase.getUser(user.username) != null)
            updateExistentUser(user)
        else
            addNewUser(user)

    fun findUser(username: String, password: String): User? {
        val maybeUser = usersDatabase.getUser(username)
        if (maybeUser?.password != password) {
            return null
        }

        val user = User(
            maybeUser.username,
            maybeUser.password,
            maybeUser.tinkoffToken
        )
        if (!tinkoffAccounts.containsKey(maybeUser.username)) {
            initServices(user)
        }
        return user
    }

    fun getTinkoffAccount(username: Username): TinkoffActualAccount? =
        tinkoffAccounts[username]

    fun getBotService(username: Username): BotService? =
        botServices[username]

    fun getAllBotServices(): List<BotService> =
        botServices.values.toList()

    // internal

    private fun initServices(user: User): Result<Unit> {
        val tinkoffAccount = initTinkoffAccount(user).getOrElse {
            return Result.failure(it)
        }
        val botService = initBotService(tinkoffAccount)

        tinkoffAccounts[user.username] = tinkoffAccount
        botServices[user.username] = botService
        return Result.success(Unit)
    }

    private fun addNewUser(user: User): Result<Unit> =
        initServices(user).onSuccess {
            usersDatabase.createUser(user.username, user.password, user.tinkoff)
        }

    private fun updateExistentUser(user: User): Result<Unit> {
        usersDatabase.setNewToken(user.username, user.tinkoff)
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

    private val tinkoffAccounts = mutableMapOf<Username, TinkoffActualAccount>()

    private val botServices = mutableMapOf<Username, BotService>()
}
