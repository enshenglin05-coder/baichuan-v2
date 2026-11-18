package com.example.myapplication01

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * 核心資料過濾策略：
 * 1. 結構或資料異常的卡片：直接捨棄（Drop），不儲存、不排錯、不佔資源。
 * 2. 遭檢舉的卡片：保留（隔離列管）。
 * 3.其餘合法卡片：正常納入前端渲染與渲染排序。
 */
object ReligionRepository {
    private const val TAG = "ReligionRepository"
    private val ttsPackets = ConcurrentHashMap<String, TtsAuditPacket>()

    // 遭檢舉卡片之隔離列管區 (Quarantine Store)
    private val quarantinedReligionCards = ConcurrentHashMap<String, ReligionCard>()

    /**
     * 儲存並同步 TTS 封包至後端倉庫
     */
    fun saveTtsPacket(packet: TtsAuditPacket) {
        try {
            Log.d(TAG, "Attempting to save TTS packet for cardId: ${packet.cardId}")
            ttsPackets[packet.cardId] = packet

            // 模擬寫入後端倉庫行為
            val json = packet.toJson()
            Log.i(TAG, "Successfully packed and saved TTS packet: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save TTS packet for cardId: ${packet.cardId}", e)
        }
    }

    /**
     * 依據語言代碼獲取經核心過濾策略處置後之合法卡片清單 (供前端渲染與渲染排序)
     */
    fun getCardsByLanguage(langCode: String): List<ReligionCard> {
        val rawCards = getRawCardsByLanguage(langCode)
        return filterAndSortCards(rawCards)
    }

    /**
     * 核心資料過濾與排序管線：
     * 1. 結構或資料異常 (isValid == false)：直接捨棄 (Drop)，不儲存、不排錯、不佔資源。
     * 2. 遭檢舉的卡片 (isReported / isQuarantined / reportCount > 0)：保留 (隔離列管)，自前端渲染清單中排除。
     * 3. 其餘合法卡片：正常納入前端渲染與渲染排序 (依類別權重與 ID 排序)。
     */
    fun filterAndSortCards(cards: List<ReligionCard>): List<ReligionCard> {
        val validCards = mutableListOf<ReligionCard>()

        for (card in cards) {
            // 策略 1：結構或資料異常的卡片：直接捨棄（Drop），不儲存、不排錯、不佔資源
            if (!card.isValid()) {
                continue
            }

            // 策略 2：遭檢舉的卡片：保留（隔離列管）
            if (card.isReported || card.isQuarantined || card.reportCount > 0) {
                quarantinedReligionCards[card.id] = card.copy(isQuarantined = true)
                continue
            }

            // 策略 3：其餘合法卡片：正常納入前端渲染與渲染排序
            validCards.add(card)
        }

        // 前端渲染排序：依據類別定義權重 (ordinal) 與 ID 排序
        return validCards.sortedWith(
            compareBy<ReligionCard> { it.category.ordinal }
                .thenBy { it.id }
        )
    }

    /**
     * 取得被隔離列管之遭檢舉卡片清單
     */
    fun getQuarantinedCards(): List<ReligionCard> {
        return quarantinedReligionCards.values.toList()
    }

    /**
     * 舉報卡片並將其移入隔離列管區
     */
    fun reportCard(card: ReligionCard) {
        if (!card.isValid()) return // 結構異常直接 Drop
        val quarantinedCard = card.copy(
            isReported = true,
            reportCount = card.reportCount + 1,
            isQuarantined = true
        )
        quarantinedReligionCards[quarantinedCard.id] = quarantinedCard
    }

    /**
     * 清空隔離區 (測試隔離用)
     */
    fun clearQuarantine() {
        quarantinedReligionCards.clear()
    }

    private fun getRawCardsByLanguage(langCode: String): List<ReligionCard> {
        return when (langCode) {
            "en" -> listOf(
                ReligionCard("en_1", R.string.religion_en_christian_title, R.string.religion_en_christian_desc, ReligionCategory.CHRISTIAN),
                ReligionCard("en_2", R.string.religion_en_catholic_title, R.string.religion_en_catholic_desc, ReligionCategory.CATHOLIC_GOLD),
                ReligionCard("en_3", R.string.religion_en_judaism_title, R.string.religion_en_judaism_desc, ReligionCategory.JUDAISM),
                ReligionCard("en_4", R.string.religion_en_islam_title, R.string.religion_en_islam_desc, ReligionCategory.ISLAM),
                ReligionCard("en_5", R.string.religion_en_general_title, R.string.religion_en_general_desc, ReligionCategory.GENERAL)
            )
            "ja" -> listOf(
                ReligionCard("ja_1", R.string.religion_ja_shinto_title, R.string.religion_ja_shinto_desc, ReligionCategory.SHINTO),
                ReligionCard("ja_2", R.string.religion_ja_buddhist_title, R.string.religion_ja_buddhist_desc, ReligionCategory.BUDDHIST_AMBER),
                ReligionCard("ja_3", R.string.religion_ja_christian_title, R.string.religion_ja_christian_desc, ReligionCategory.CHRISTIAN),
                ReligionCard("ja_4", R.string.religion_ja_catholic_title, R.string.religion_ja_catholic_desc, ReligionCategory.CATHOLIC_BLUE),
                ReligionCard("ja_5", R.string.religion_ja_general_title, R.string.religion_ja_general_desc, ReligionCategory.GENERAL)
            )
            "ko" -> listOf(
                ReligionCard("ko_1", R.string.religion_ko_protestant_title, R.string.religion_ko_protestant_desc, ReligionCategory.CHRISTIAN),
                ReligionCard("ko_2", R.string.religion_ko_catholic_title, R.string.religion_ko_catholic_desc, ReligionCategory.CATHOLIC_BLUE),
                ReligionCard("ko_3", R.string.religion_ko_buddhist_title, R.string.religion_ko_buddhist_desc, ReligionCategory.BUDDHIST_GOLD),
                ReligionCard("ko_4", R.string.religion_ko_general_title, R.string.religion_ko_general_desc, ReligionCategory.GENERAL)
            )
            "vi" -> listOf(
                ReligionCard("vi_1", R.string.religion_vi_buddhist_title, R.string.religion_vi_buddhist_desc, ReligionCategory.BUDDHIST_GOLD),
                ReligionCard("vi_2", R.string.religion_vi_catholic_title, R.string.religion_vi_catholic_desc, ReligionCategory.CHRISTIAN),
                ReligionCard("vi_3", R.string.religion_vi_caodai_title, R.string.religion_vi_caodai_desc, ReligionCategory.COMPASSION),
                ReligionCard("vi_4", R.string.religion_vi_general_title, R.string.religion_vi_general_desc, ReligionCategory.GENERAL)
            )
            else -> listOf( // zh-TW
                ReligionCard("tw_1", R.string.religion_tw_tudigong_title, R.string.religion_tw_tudigong_desc, ReligionCategory.ELDER),
                ReligionCard("tw_2", R.string.religion_tw_mazu_title, R.string.religion_tw_mazu_desc, ReligionCategory.COMPASSION),
                ReligionCard("tw_3", R.string.religion_tw_guangong_title, R.string.religion_tw_guangong_desc, ReligionCategory.WARRIOR),
                ReligionCard("tw_4", R.string.religion_tw_christian_title, R.string.religion_tw_christian_desc, ReligionCategory.CHRISTIAN),
                ReligionCard("tw_5", R.string.religion_tw_buddhist_title, R.string.religion_tw_buddhist_desc, ReligionCategory.BUDDHIST_AMBER),
                ReligionCard("tw_6", R.string.religion_tw_general_title, R.string.religion_tw_general_desc, ReligionCategory.GENERAL)
            )
        }
    }
}
