package backend.bot

import backend.strategy.HyperParameterUid
import backend.strategy.StrategyUid

class BotCannotBeLoaded(name: String, uid: StrategyUid) :
    Exception("Can't load a bot with name=$name and strategy=$uid")

class HyperParameterCannotBeLoaded(botId: BotUid, paramId: HyperParameterUid) :
    Exception("Can't load a hyper parameter $paramId for bot $botId")

class BotNotFoundException(uid: BotUid) : Exception("Bot not found: $uid")

class ExpectedHyperParameterException(uid: HyperParameterUid) : Exception("Expected hyper parameter: $uid")

class ParseHyperParameterException(uid: HyperParameterUid, str: String) :
    Exception("Can't parse hyper parameter with id $uid: $str")