package backend.tinkoff.service

import backend.common.model.SecurityInfo
import backend.tinkoff.model.Figi

class TinkoffInfoService {

    fun getSecurities(): Result<List<SecurityInfo>> =
        Result.success(securities)

    fun getFigiById(id: Int): Result<Figi> {
        val securityInfo = securities.firstOrNull { securityInfo ->
            securityInfo.id == id
        }
        return securityInfo?.let { Result.success(it.figi) }
            ?: Result.failure(Exception("No such id"))
    }

    fun getIdByFigi(figi: Figi): Result<Int> {
        val securityInfo = securities.firstOrNull { securityInfo ->
            securityInfo.figi == figi
        }
        return securityInfo?.let { Result.success(it.id) }
            ?: Result.failure(Exception("No such figi"))
    }

    // internal

    private val securities = listOf(
        SecurityInfo(0, "Нижнекамскнефтехим", "BBG000GQSRR5"),
        SecurityInfo(1, "Яндекс", "BBG006L8G4H1"),
        SecurityInfo(2, "Сбер", "BBG004730N88"),
        SecurityInfo(3, "Северсталь", "BBG000Q3XWC4"),
        SecurityInfo(4, "TCS Group", "BBG00QPYJ5H0"),
        SecurityInfo(5, "Детский Мир", "BBG000BN56Q9"),
        SecurityInfo(6, "VK", "BBG00178PGX3"),
        SecurityInfo(7, "ФосАгро", "BBG004S689R0"),
        SecurityInfo(8, "ГДР Лента", "BBG000QQPXZ5"),
        SecurityInfo(9, "Магнит", "BBG004RVFCY3"),
    )
}
