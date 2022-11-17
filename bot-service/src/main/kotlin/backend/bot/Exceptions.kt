package backend.bot

import backend.strategy.HyperParameterUid

class BotNotFoundException(uid: BotUid) : Exception("Bot not found: $uid")

class ExpectedHyperParameterException(uid: HyperParameterUid) : Exception("Expected hyper parameter: $uid")

class ParseHyperParameterException(uid: HyperParameterUid, str: String) :
    Exception("Can't parse hyper parameter with id $uid: $str")