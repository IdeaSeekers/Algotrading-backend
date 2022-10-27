package backend.bot

import backend.bot.api.BotUid

class BotNotFoundException(uid: BotUid) : java.lang.Exception("Bot not found: $uid")

class ContainerStartException() : java.lang.Exception("Can't start container")