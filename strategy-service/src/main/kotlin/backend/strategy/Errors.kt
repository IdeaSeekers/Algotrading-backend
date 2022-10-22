package backend.strategy

class UnsupportedStrategyException(uid: StrategyUid) : java.lang.Exception("Unsupported strategy: $uid")