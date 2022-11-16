package backend.server

import backend.common.model.HyperParameterInfo
import backend.common.model.StrategyInfo

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