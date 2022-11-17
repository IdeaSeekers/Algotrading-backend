package backend.strategy

class UnsupportedStrategyException(uid: StrategyUid) : Exception("Unsupported strategy: $uid")

class UnknownHyperParameterException(uid: HyperParameterUid) : Exception("Unknown hyperParameter: $uid")