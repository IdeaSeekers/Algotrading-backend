package backend.common.model

object Info {
    val figiHyperParameterInfo = HyperParameterInfo(
        "figi",
        "figi",
        HyperParameterInfo.Type.STRING,
    )

    val balanceHyperParameterInfo = HyperParameterInfo(
        "balance",
        "initial balance",
        HyperParameterInfo.Type.FLOAT,
        min = 0.0,
    )

    val simpleStrategyInfo = StrategyInfo(
        "Simple strategy",
        "Super mega hyper ultra stupid strategy",
        StrategyInfo.Risk.HIGH,
        listOf(Id.figiHyperParameterUid, Id.balanceHyperParameterUid),
    )
}