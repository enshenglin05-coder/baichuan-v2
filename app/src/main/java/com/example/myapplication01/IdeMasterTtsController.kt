package com.example.myapplication01

import android.content.Context
import android.util.Log

/**
 * 唯一主控與編譯輸入控制器 (IDE Master TTS Controller)
 * 供開發者在 IDE 中直接透過原生 Kotlin 程式碼進行編譯設定與執行核心運作流程：
 *
 * 1. 【唯一主控與編譯輸入】 (IDE 控管)：
 *    - 所有要播報的文字內容 (CONFIG_TEXT_TO_SPEAK)
 *    - 目標經緯度 (CONFIG_TARGET_LATITUDE, CONFIG_TARGET_LONGITUDE)
 *    - 套用距離範圍 (CONFIG_RADIUS_KM)
 *    - 初始與剩餘播放次數 (CONFIG_INITIAL_COUNT)
 *
 * 2. 【核心運作流程】 (播一次、拉一次、扣一次)：
 *    - 經緯度比對成功後進行 TTS 播報。
 *    - 鎖定播報內容的 「倒數第二個字」 瞬間，精準觸發次數扣減 (10 ➔ 9 ➔ ...)。
 *    - 歸零自動搬運（屍體歸位）：當次數扣減至 0 歸零時，系統將該張卡片資料實體從主資料夾搬移、推送到專屬已封存/回收倉庫存放。
 */
object IdeMasterTtsController {

    // =========================================================================
    // 1. 唯一主控與編譯輸入（IDE 控管）設定區
    //    開發者可以在 IDE 中直接修改以下常數與參數進行編譯與實體環境測試
    // =========================================================================
    var CONFIG_TEXT_TO_SPEAK: String = "新北市中和區廣濟宮聖王公守護你"
    var CONFIG_TARGET_LATITUDE: Double = 25.002528
    var CONFIG_TARGET_LONGITUDE: Double = 121.497528
    var CONFIG_RADIUS_KM: Double = 10.0
    var CONFIG_INITIAL_COUNT: Int = 100
    var CONFIG_CARD_ID: String = "GANGJIGONG_100_COUNT_CARD"

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
     * 2. 核心運作流程 (播一次、拉一次、扣一次) 實體執行入口：
     * - GPS 經緯度距離比對
     * - 鎖定播報內容的「倒數第二個字」瞬間進行精準觸發與次數扣減
     * - 次數歸零時自動將主資料夾的實體卡片檔案搬移（屍體歸位）至專屬回收倉庫
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
        val isWithinRange = distance <= CONFIG_RADIUS_KM

        val logMsg = "📍 [經緯度比對] 目標: ($CONFIG_TARGET_LATITUDE, $CONFIG_TARGET_LONGITUDE), 當前: ($currentLat, $currentLng), 計算距離: ${String.format(java.util.Locale.getDefault(), "%.3f", distance)} km, 在範圍內: $isWithinRange"
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
            ad = compileConfig(context = context)
        }

        if (!ad.isPlayable) {
            try {
                Log.w("IdeMasterController", "⚠️ [卡片無效/已歸零] 卡片 [${ad.id}] 剩餘次數為 0 或非 ACTIVE 狀態。")
            } catch (_: Throwable) {
                println("[IdeMasterController] ⚠️ [卡片無效/已歸零] 卡片 [${ad.id}] 剩餘次數為 0 或非 ACTIVE 狀態。")
            }
            return false
        }

        // 步驟 B: 鎖定播報內容的「倒數第二個字」瞬間，精準觸發次數扣減
        val textToSpeak = ad.text.ifBlank { CONFIG_TEXT_TO_SPEAK }
        val secondToLastChar = if (textToSpeak.length >= 2) textToSpeak[textToSpeak.length - 2] else ' '

        // 執行次數扣減 (播一次、拉一次、扣一次)
        val resultPair = TtsAdManager.onAdPlayed(CONFIG_CARD_ID, context)
        if (resultPair != null) {
            val (updatedAd, archiveRecord) = resultPair
            val remainingCount = updatedAd.remainingCount
            val triggerLog = "🎯 [倒數第二字精準觸發] 鎖定倒數第二字 '$secondToLastChar'，剩餘次數扣減: ${ad.remainingCount} ➔ $remainingCount"
            try {
                Log.d("IdeMasterController", triggerLog)
                Log.d("CARD_COUNT", "🔥 倒數第二字觸發成功！當前剩餘次數: " + remainingCount)
            } catch (_: Throwable) {
                println("[IdeMasterController] $triggerLog")
                println("[CARD_COUNT] 🔥 倒數第二字觸發成功！當前剩餘次數: " + remainingCount)
            }

            onSecondToLastCharTriggered?.invoke(updatedAd.remainingCount, secondToLastChar)

            // 步驟 C: 歸零自動搬運（屍體歸位）
            if ((updatedAd.status == TTSAdStatus.ARCHIVED_ZERO) || (updatedAd.remainingCount <= 0)) {
                // 將該張已播完的卡片資料實實在在地從主資料夾 (local_cards/) 搬移、推送到專屬的已封存/回收倉庫 (archived_cards/)
                LocalCardDatabase.archiveCardFile(CONFIG_CARD_ID, context)
                BaichuanRepository.removeCard(CONFIG_CARD_ID)

                if (archiveRecord != null) {
                    val archiveLog = "📦 [屍體歸位完成] 卡片 [$CONFIG_CARD_ID] 次數已歸零！實體檔案已成功搬移至專屬回收倉庫，ArchiveID: ${archiveRecord.archiveId}"
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
