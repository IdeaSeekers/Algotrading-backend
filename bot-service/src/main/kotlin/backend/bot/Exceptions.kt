package backend.bot

class BotNotFoundException(uid: BotUid) : java.lang.Exception("Bot not found: $uid")

class ContainerStartException() : java.lang.Exception("Can't start container")