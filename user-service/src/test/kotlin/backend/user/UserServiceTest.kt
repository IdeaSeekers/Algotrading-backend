package backend.user

import backend.bot.BotService
import backend.common.model.User
import backend.db.bots.BotsDatabase
import backend.db.bots.UsersDatabase
import backend.strategy.StrategyService
import backend.tinkoff.account.TinkoffActualAccount
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock

internal class UserServiceTest {

    private val tinkoffAccountMock = mock<TinkoffActualAccount>()
    private val botServiceMock = mock<BotService>()
    private val usersDatabaseMock = mock<UsersDatabase>()
    private val botsDatabaseMock = mock<BotsDatabase>()
    private val strategyServiceMock = mock<StrategyService>()

    private val userService = UserServiceMock(tinkoffAccountMock, botServiceMock, usersDatabaseMock, botsDatabaseMock, strategyServiceMock)


    @Nested
    @DisplayName("addUser")
    inner class AddUserTest {

        @Test
        fun `addUser should register user`() {
            `when`(usersDatabaseMock.getUser(userNikita.username)).thenReturn(null)

            val result = userService.addUser(userNikita)
            assertTrue(result.isSuccess)
        }

        @Test
        fun `addUser should fail if tinkoff fails`() {
            `when`(usersDatabaseMock.getUser(userFailed.username)).thenReturn(null)

            val result = userService.addUser(userFailed)
            assertTrue(result.isFailure)
        }
    }

    @Nested
    @DisplayName("findUser")
    inner class FindUserTest {

        @Test
        fun `findUser should return registered user`() {
            `when`(usersDatabaseMock.getUser(userNikita.username)).thenReturn(userNikita.toDatabase())

            val maybeUser = userService.findUser(userNikita.username, userNikita.password)
            assertEquals(userNikita, maybeUser)
        }

        @Test
        fun `findUser should return all (and only) registered users`() {
            `when`(usersDatabaseMock.getUser(userNikita.username)).thenReturn(userNikita.toDatabase())
            `when`(usersDatabaseMock.getUser(userBagrorg.username)).thenReturn(userBagrorg.toDatabase())
            `when`(usersDatabaseMock.getUser(userFailed.username)).thenReturn(null)

            val maybeUserNikita = userService.findUser(userNikita.username, userNikita.password)
            val maybeUserBagrorg = userService.findUser(userBagrorg.username, userBagrorg.password)
            val maybeUserFailed = userService.findUser(userFailed.username, userFailed.password)

            assertEquals(userNikita, maybeUserNikita)
            assertEquals(userBagrorg, maybeUserBagrorg)
            assertNull(maybeUserFailed)
        }

        @Test
        fun `findUser should return null for unregistered user`() {
            userService.addUser(userBagrorg)
            val maybeUser = userService.findUser(userNikita.username, userNikita.password)
            assertNull(maybeUser)
        }
    }

    @Nested
    @DisplayName("getTinkoffAccount")
    inner class GetTinkoffAccountTest {

        @Test
        fun `should return account for registered user`() {
            userService.addUser(userBagrorg)
            val maybeAccount = userService.getTinkoffAccount(userBagrorg.username)
            assertEquals(tinkoffAccountMock, maybeAccount)
        }

        @Test
        fun `should return null for unregistered user`() {
            userService.addUser(userBagrorg)
            val maybeUser = userService.getTinkoffAccount(userNikita.username)
            assertNull(maybeUser)
        }
    }

    @Nested
    @DisplayName("getBotService")
    inner class GetBotServiceTest {

        @Test
        fun `should return instance for registered user`() {
            userService.addUser(userBagrorg)
            val maybeInstance = userService.getBotService(userBagrorg.username)
            assertEquals(botServiceMock, maybeInstance)
        }

        @Test
        fun `should return null for unregistered user`() {
            userService.addUser(userBagrorg)
            val maybeInstance = userService.getBotService(userNikita.username)
            assertNull(maybeInstance)
        }
    }

    // data

    private val userNikita = User("nikita", "cherepovets", "token1")
    private val userBagrorg = User("bagrorg", "bagrorg's life", "token2")
    private val userFailed = User("fail", "fail", "fail")

    // internal

    fun User.toDatabase() =
        UsersDatabase.DatabaseUser(this.username, this.password, this.tinkoff)

    inner class UserServiceMock(
        private val tinkoffAccount: TinkoffActualAccount,
        private val botService: BotService,
        usersDatabaseMock: UsersDatabase,
        botsDatabaseMock: BotsDatabase,
        strategyService: StrategyService,
    ) : UserService(usersDatabaseMock, botsDatabaseMock, strategyService) {

        override fun initTinkoffAccount(user: User) =
            if (user.tinkoff == "fail")
                Result.failure(Exception(""))
            else
                Result.success(tinkoffAccount)

        override fun initBotService(tinkoffAccount: TinkoffActualAccount) = botService
    }
}