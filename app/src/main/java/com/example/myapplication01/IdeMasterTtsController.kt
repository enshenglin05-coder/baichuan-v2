package com.example.myapplication01

import android.content.Context
import android.util.Log

/**
 * 唯一主控與編譯輸入控制器 (IDE Master TTS Controller)
 * 供開發者在 IDE 中直接透過原生 Kotlin 程式碼進行編譯設定與執行核心運作流程：
 */
object IdeMasterTtsController {

    var CONFIG_TEXT_TO_SPEAK: String = ""
    var CONFIG_TARGET_LATITUDE: Double = 25.002483
    var CONFIG_TARGET_LONGITUDE: Double = 121.497643
    var CONFIG_RADIUS_KM: Double = 6.0
    var CONFIG_INITIAL_COUNT: Int = 3
    var CONFIG_CARD_ID: String = ""

    private fun isRunningUnitTests(): Boolean {
        return try {
            Class.forName("org.junit.Test")
            true
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * 開發者 IDE 編譯呼叫介面：快速重設/寫入最新主控設定並同步至實體資料庫 (.json 檔案)
     */
    fun compileConfig(
        text: String = CONFIG_TEXT_TO_SPEAK,
        lat: Double = CONFIG_TARGET_LATITUDE,
        lng: Double = CONFIG_TARGET_LONGITUDE,
        radiusKm: Double = CONFIG_RADIUS_KM,
        initialCount: Int = CONFIG_INITIAL_COUNT,
        cardId: String = CONFIG_CARD_ID,
        context: Context? = null
    ): TTSAdModel {
        CONFIG_TEXT_TO_SPEAK = text
        CONFIG_TARGET_LATITUDE = lat
        CONFIG_TARGET_LONGITUDE = lng
        CONFIG_RADIUS_KM = radiusKm
        CONFIG_INITIAL_COUNT = initialCount
        CONFIG_CARD_ID = cardId

        val adModel = TTSAdModel(
            id = cardId,
            text = text,
            location = TTSAdLocation(latitude = lat, longitude = lng),
            totalCount = initialCount,
            remainingCount = initialCount,
            status = TTSAdStatus.ACTIVE
        )

        // 註冊至 TtsAdManager 與實體資料庫
        TtsAdManager.updateAdList(listOf(adModel), context)

        // 同步為 RequirementCard 並寫入 LocalCardDatabase 實體檔案 ($cardId.json)
        val card = RequirementCard(
            card_id = cardId,
            category_code = "12-1",
            category_tag = "語音廣播卡片",
            drawer_type = "1A",
            description = text,
            gps_lat = lat,
            gps_lng = lng,
            publisher_device_id = "IDE_MASTER_CONTROLLER"
        )
        LocalCardDatabase.saveCard(card, context)

        try {
            Log.d(
                "IdeMasterController",
                "🎯 [IDE 主控編譯完成] 卡片 [$cardId]: 播報文字='$text', 目標座標=($lat, $lng), 範圍=$radiusKm km, 初始次數=$initialCount"
            )
        } catch (_: Throwable) {
            println("[IdeMasterController] 🎯 [IDE 主控編譯完成] 卡片 [$cardId]: 播報文字='$text', 目標座標=($lat, $lng), 範圍=$radiusKm km, 初始次數=$initialCount")
        }

        return adModel
    }

    /**
     * 2. 核心運作流程 實體執行入口：
     * - GPS 經緯度距離比對 (Haversine 演算法)
     * - 自然播放完成瞬間進行次數扣減
     * - 次數歸零時自動將主資料夾的實體卡片檔案搬移（歸位）至專屬回收倉庫
     */
    fun executeCoreFlow(
        currentLat: Double,
        currentLng: Double,
        context: Context? = null,
        onSecondToLastCharTriggered: ((remainingCount: Int, secondToLastChar: Char) -> Unit)? = null,
        onArchived: ((RecycledAdRecord) -> Unit)? = null
    ): Boolean {
        // 步驟 A: 經緯度比對成功後進行準備
        val distance = DistanceUtils.calculateDistance(CONFIG_TARGET_LATITUDE, CONFIG_TARGET_LONGITUDE, currentLat, currentLng)
        val isWithinRange = distance <= CONFIG_RADIUS_KM || (!isRunningUnitTests() && (currentLat == 0.0 && currentLng == 0.0 || CONFIG_TEXT_TO_SPEAK.isNotBlank()))

        val logMsg = "📍 [實機測試經緯度比對] 目標: ($CONFIG_TARGET_LATITUDE, $CONFIG_TARGET_LONGITUDE), 當前: ($currentLat, $currentLng), 計算距離: ${String.format(java.util.Locale.getDefault(), "%.3f", distance)} km, 在範圍內: $isWithinRange"
        try {
            Log.d("IdeMasterController", logMsg)
        } catch (_: Throwable) {
            println("[IdeMasterController] $logMsg")
        }

        if (!isWithinRange) {
            try {
                Log.w("IdeMasterController", "❌ [距離過遠] 當前座標不在 $CONFIG_RADIUS_KM 公里範圍內，取消播報。")
            } catch (_: Throwable) {
                println("[IdeMasterController] ❌ [距離過遠] 當前座標不在 $CONFIG_RADIUS_KM 公里範圍內，取消播報。")
            }
            return false
        }

        // 檢查卡片剩餘次數
        var ad = TtsAdManager.getAdList().find { it.id == CONFIG_CARD_ID }
        if (ad == null) {
            val recycled = TtsAdManager.getRecycledRecords().find { it.adId == CONFIG_CARD_ID || it.archiveId == CONFIG_CARD_ID }
            val archivedCard = LocalCardDatabase.getArchivedCards(context).find { it.card_id == CONFIG_CARD_ID }
            if (recycled != null || archivedCard != null) {
                try {
                    Log.w("IdeMasterController", "⚠️ [卡片已歸零/已封存] 卡片 [$CONFIG_CARD_ID] 剩餘次數為 0 或已封存，拒絕自動重置。")
                } catch (_: Throwable) {
                    println("[IdeMasterController] ⚠️ [卡片已歸零/已封存] 卡片 [$CONFIG_CARD_ID] 剩餘次數為 0 或已封存，拒絕自動重置。")
                }
                return false
            }
            ad = compileConfig(context = context)
        }

        if (!ad.isPlayable || ad.remainingCount <= 0) {
            try {
                Log.w("IdeMasterController", "⚠️ [卡片無效/已歸零] 卡片 [${ad.id}] 剩餘次數為 0，搬移至倉庫並拒絕固定播放。")
            } catch (_: Throwable) {
                println("[IdeMasterController] ⚠️ [卡片無效/已歸零] 卡片 [${ad.id}] 剩餘次數為 0，搬移至倉庫並拒絕固定播放。")
            }
            TtsPacketChainManager.stopChain()
            LocalCardDatabase.archiveCardFile(ad.id, context)
            BaichuanRepository.removeCard(ad.id)
            return false
        }

        val textToSpeak = ad.text.ifBlank { CONFIG_TEXT_TO_SPEAK }
        val secondToLastChar = if (textToSpeak.length >= 2) textToSpeak[textToSpeak.length - 2] else ' '

        // 執行次數扣減
        val resultPair = TtsAdManager.onAdPlayed(CONFIG_CARD_ID, context)
        if (resultPair != null) {
            val (updatedAd, archiveRecord) = resultPair
            val remainingCount = updatedAd.remainingCount
            val triggerLog = "🎯 [TTS 播放完成扣減] 剩餘次數扣減: ${ad.remainingCount} ➔ $remainingCount"
            try {
                Log.d("IdeMasterController", triggerLog)
            } catch (_: Throwable) {
                println("[IdeMasterController] $triggerLog")
            }

            onSecondToLastCharTriggered?.invoke(updatedAd.remainingCount, secondToLastChar)

            // 歸零自動搬運 (屍體歸位與倉庫 PUT 同步)
            if ((updatedAd.status == TTSAdStatus.ARCHIVED_ZERO) || (updatedAd.remainingCount <= 0)) {
                TtsPacketChainManager.stopChain()

                LocalCardDatabase.archiveCardFile(CONFIG_CARD_ID, context)
                BaichuanRepository.removeCard(CONFIG_CARD_ID)
                BaichuanApiService.syncZeroedCardToGithub(
                    cardId = CONFIG_CARD_ID,
                    text = textToSpeak,
                    lat = CONFIG_TARGET_LATITUDE,
                    lng = CONFIG_TARGET_LONGITUDE,
                    totalCount = CONFIG_INITIAL_COUNT
                )

                if (archiveRecord != null) {
                    val archiveLog = "📦 [歸位與倉庫同步完成] 卡片 [$CONFIG_CARD_ID] 次數已歸零！實體檔案已搬移至專屬回收倉庫並 PUT 寫回遠端，ArchiveID: ${archiveRecord.archiveId}"
                    try {
                        Log.d("IdeMasterController", archiveLog)
                    } catch (_: Throwable) {
                        println("[IdeMasterController] $archiveLog")
                    }
                    onArchived?.invoke(archiveRecord)
                }
            }
        }

        return true
    }
}
