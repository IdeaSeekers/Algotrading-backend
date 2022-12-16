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
        "Simple Strategy",
        "Покупаем, когда цена растет. Продаем, когда падает.",
        StrategyInfo.Risk.HIGH,
        listOf(Id.figiHyperParameterUid, Id.balanceHyperParameterUid),
    )

    val risingThreeMethodStrategyInfo = StrategyInfo(
        "Rising Three Methods",
        "Cмотрим на историю каждую минуту. Внутри минуты делаем каждые 10 секунд измерения." +
                "Делая так четыре минуты подряд, получаем 4 чанка по 6 измерений. " +
                "Посмотрим на это как на японские свечи. Если так случилось, что первая свеча белая, " +
                "а потом три чёрных, при этом объём белой свечи больше суммы объёмов чёрных свечей, " +
                "то пятая свеча вероятно будет белой.",
        StrategyInfo.Risk.HIGH,
        listOf(Id.figiHyperParameterUid, Id.balanceHyperParameterUid),
    )
}