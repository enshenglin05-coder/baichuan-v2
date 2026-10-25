package com.example.myapplication01

import com.google.gson.Gson
import com.google.gson.JsonParser
import java.util.concurrent.ConcurrentHashMap

/**
 * 隔離與凍結審核紀錄 (Quarantined Card Record)
 */
data class QuarantinedCardRecord(
    val recordId: String,
    val rawJson: String,
    val errorCount: Int,
    val reportCount: Int,
    val reason: String,
    var status: ReviewStatus = ReviewStatus.MANUAL_REVIEW,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class ReviewStatus {
        MANUAL_REVIEW, // 帶入人工審核
        FROZEN,        // 凍結
        IGNORED,       // 忽略
        APPROVED       // 人工審核通過
    }
}

/**
 * 百川卡片 JSON 異常處理與 12 小時生命週期風控模組
 * 核心規則：
 * 1. 12 小時自動暫存與抹除：超過 12 小時的卡片資料自動清除/抹除。
 * 2. JSON 格式異常預設直接丟棄 (略過該筆資料，不額外比對立案)。
 * 3. 例外規則：被檢舉達 3 次或相同問題/異常發生 3 次者，改為另存格式、帶入人工審核、並支援忽略或凍結。
 */
object CardJsonExceptionHandler {

    private val gson = Gson()
    private const val EXCEPTION_THRESHOLD = 3

    // 紀錄異常問題發生次數 (以 cardId 或 JSON Hash 為 key)
    private val errorOccurrencesMap = ConcurrentHashMap<String, Int>()

    // 人工審核隔離區 (Quarantine Store)
    private val quarantinedRecords = ConcurrentHashMap<String, QuarantinedCardRecord>()

    private fun safeLogD(tag: String, msg: String) {
        try { android.util.Log.d(tag, msg) } catch (_: Throwable) {}
    }
    private fun safeLogW(tag: String, msg: String) {
        try { android.util.Log.w(tag, msg) } catch (_: Throwable) {}
    }
    private fun safeLogE(tag: String, msg: String) {
        try { android.util.Log.e(tag, msg) } catch (_: Throwable) {}
    }

    /**
     * 安全解析卡片 JSON 字串並套用風控與過期規則
     */
    fun parseAndHandle(jsonString: String, identifierHint: String? = null): List<RequirementCard> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return emptyList()

        val parsedCards = mutableListOf<RequirementCard>()
        val key = identifierHint?.takeIf { it.isNotBlank() } ?: calculateHashKey(trimmed)

        try {
            val jsonElement = JsonParser.parseString(trimmed)
            if (jsonElement.isJsonArray) {
                val array = jsonElement.asJsonArray
                for (elem in array) {
                    val card = parseSingleElement(elem)
                    if (card != null) {
                        parsedCards.add(card)
                    }
                }
            } else if (jsonElement.isJsonObject) {
                val card = parseSingleElement(jsonElement)
                if (card != null) {
                    parsedCards.add(card)
                }
            } else {
                throw IllegalArgumentException("JSON 格式既非物件亦非陣列")
            }
        } catch (e: Exception) {
            val currentErrors = errorOccurrencesMap.compute(key) { _, count -> (count ?: 0) + 1 } ?: 1
            safeLogW("CardJsonHandler", "⚠️ JSON 格式解析異常 [代號: $key, 累計次數: $currentErrors]: ${e.message}")

            if (currentErrors >= EXCEPTION_THRESHOLD) {
                // 例外規則：相同問題發生 3 次，另存格式、帶入人工審核、支援忽略或凍結
                quarantineExceptionRecord(
                    recordId = key,
                    rawJson = trimmed,
                    errorCount = currentErrors,
                    reportCount = 0,
                    reason = "JSON 格式異常累計達 $currentErrors 次: ${e.message}"
                )
            } else {
                // 直接丟棄（略過該筆資料）
                safeLogD("CardJsonHandler", "🗑️ JSON 格式異常單次發生 (<3次)，直接丟棄略過 [$key]")
            }
            return emptyList()
        }

        // 針對解析成功的卡片進行第二階段風控與過期過濾
        val validCards = mutableListOf<RequirementCard>()
        val now = System.currentTimeMillis()

        for (card in parsedCards) {
            // 12 小時自動暫存與抹除檢查
            if (card.isExpired(now)) {
                safeLogD("CardJsonHandler", "⌛ 卡片已超過 12 小時暫存期限，自動抹除: ${card.card_id}")
                continue
            }

            // 檢舉達 3 次例外規則檢查
            if (card.reporter_ids.size >= EXCEPTION_THRESHOLD || card.moderation_status == "AUTO_HIDDEN") {
                quarantineExceptionRecord(
                    recordId = card.card_id.ifBlank { key },
                    rawJson = gson.toJson(card),
                    errorCount = 0,
                    reportCount = card.reporter_ids.size,
                    reason = "檢舉次數累計達 ${card.reporter_ids.size} 次 (閾值 $EXCEPTION_THRESHOLD)"
                )
                continue
            }

            validCards.add(card)
        }

        return validCards
    }

    private fun parseSingleElement(elem: com.google.gson.JsonElement): RequirementCard? {
        if (!elem.isJsonObject) return null
        val obj = elem.asJsonObject
        val card = gson.fromJson(obj, RequirementCard::class.java) ?: return null
        if (card.card_id.isBlank()) return null
        return card
    }

    private fun quarantineExceptionRecord(
        recordId: String,
        rawJson: String,
        errorCount: Int,
        reportCount: Int,
        reason: String
    ) {
        val existing = quarantinedRecords[recordId]
        val updatedRecord = QuarantinedCardRecord(
            recordId = recordId,
            rawJson = rawJson,
            errorCount = maxOf(errorCount, existing?.errorCount ?: 0),
            reportCount = maxOf(reportCount, existing?.reportCount ?: 0),
            reason = reason,
            status = existing?.status ?: QuarantinedCardRecord.ReviewStatus.MANUAL_REVIEW
        )
        quarantinedRecords[recordId] = updatedRecord
        safeLogE("CardJsonHandler", "🚨 已將問題卡片帶入人工審核隔離區 [ID: $recordId, 狀態: ${updatedRecord.status}]: $reason")
    }

    private fun calculateHashKey(json: String): String {
        return "JSON_ERR_" + Math.abs(json.hashCode()).toString(16)
    }

    /**
     * 將被檢舉達門檻的卡片帶入人工審核隔離區 (支援同選項 3 次自動隱藏後台同步)
     */
    fun quarantineReportedCard(card: RequirementCard, reason: String, reportCount: Int) {
        quarantineExceptionRecord(
            recordId = card.card_id,
            rawJson = gson.toJson(card),
            errorCount = 0,
            reportCount = reportCount,
            reason = "同選項檢舉次數累計達 $reportCount 次 (檢舉選項: $reason)"
        )
    }

    /**
     * 取得所有待人工審核/凍結的紀錄
     */
    fun getQuarantinedRecords(): List<QuarantinedCardRecord> {
        return quarantinedRecords.values.toList()
    }

    /**
     * 更新人工審核狀態 (支援 忽略 IGNORED / 凍結 FROZEN / 放行 APPROVED)
     */
    fun updateRecordStatus(recordId: String, status: QuarantinedCardRecord.ReviewStatus) {
        quarantinedRecords[recordId]?.let {
            it.status = status
            safeLogD("CardJsonHandler", "⚙️ 已更新審核紀錄 [$recordId] 狀態為: $status")
        }
    }

    /**
     * 記錄單一解析/格式異常 (增加異常次數)
     */
    fun reportJsonException(identifier: String, rawJson: String, errorMessage: String) {
        val count = errorOccurrencesMap.compute(identifier) { _, current -> (current ?: 0) + 1 } ?: 1
        if (count >= EXCEPTION_THRESHOLD) {
            quarantineExceptionRecord(
                recordId = identifier,
                rawJson = rawJson,
                errorCount = count,
                reportCount = 0,
                reason = "JSON 異常發生達 $count 次: $errorMessage"
            )
        }
    }

    /**
     * 清空隔離與記錄 (測試隔離用)
     */
    fun clearAll() {
        errorOccurrencesMap.clear()
        quarantinedRecords.clear()
    }
}
