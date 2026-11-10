package com.example.myapplication01

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 廣濟宮開漳聖王 TTS 倒數與範圍觸發管理器
 * 簡化版：前台手動觸發（發布/刷新）、6KM Haversine 距離比對、FIFO 佇列、無字數監聽器、播放完畢扣減倒數與歸零封存
 */
object HurlungchuangTestManager {

    // 1. 目標位置設定：25.002457, 121.497370
    const val TARGET_LATITUDE = 25.002457
    const val TARGET_LONGITUDE = 121.497370
    
    // 2. 範圍判定 (6 公里內)
    const val RADIUS_KM = 6.0

    // 3. 播報內容
    const val TARGET_TEXT = "中和廣濟宮開漳聖王聖王公守護您"

    // 4. 次數倒數與歸零設定 (100次播放)
    private var remainingCount = 100

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }

    private fun safeLog(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    /**
     * 執行位置計算與 6 公里範圍判定 (Haversine 距離演算法)
     */
    fun checkLocationAndDistance(currentLat: Double, currentLng: Double): Boolean {
        val timestamp = getCurrentTimestamp()
        val distance = DistanceUtils.calculateDistance(TARGET_LATITUDE, TARGET_LONGITUDE, currentLat, currentLng)
        val isWithinRange = distance <= RADIUS_KM

        safeLog(
            "TTS_DEBUG",
            "[$timestamp] [Haversine 經緯度比對] 目標: ($TARGET_LATITUDE, $TARGET_LONGITUDE), 當前: ($currentLat, $currentLng), 計算距離: ${String.format(Locale.getDefault(), "%.3f", distance)} km, 在 6KM 範圍內: $isWithinRange, 當前剩餘次數: $remainingCount"
        )

        return isWithinRange
    }

    /**
     * 使用者點擊「發布」或「刷新」時觸發：
     * 帶入使用者座標，進行 6KM Geospatial 檢查，符合條件即拉取 payload 並入佇列
     */
    fun checkAndPullPayload(currentLat: Double, currentLng: Double): String? {
        val timestamp = getCurrentTimestamp()
        val isWithin = checkLocationAndDistance(currentLat, currentLng)

        if (!isWithin) {
            val distance = DistanceUtils.calculateDistance(TARGET_LATITUDE, TARGET_LONGITUDE, currentLat, currentLng)
            safeLog("TTS_DEBUG", "[$timestamp] 距離超過 6 KM 範圍 (${String.format(Locale.getDefault(), "%.3f", distance)} km)，不拉取 TTS 語音封包。")
            return null
        }

        if (remainingCount <= 0) {
            safeLog("TTS_DEBUG", "[$timestamp] TTS 剩餘播放次數已歸零 ($remainingCount)，不再拉取語音封包。")
            return null
        }

        safeLog("TTS_DEBUG", "[$timestamp] 🎯 [6KM 觸發成功] 拉取語音封包: '$TARGET_TEXT' (當前剩餘次數: $remainingCount)")
        return TARGET_TEXT
    }

    /**
     * TTS 語音自然播放完畢 (onDone) 後呼叫：執行次數扣減與歸零封存
     */
    fun onPlaybackCompleted(onArchived: ((RecycledAdRecord) -> Unit)? = null): Boolean {
        if (remainingCount <= 0) {
            safeLog("TTS_DEBUG", "[${getCurrentTimestamp()}] 剩餘次數已為 0，無需重複扣減。")
            return false
        }

        remainingCount--
        val timestamp = getCurrentTimestamp()

        safeLog("CARD_COUNT", "🔥 [TTS 自然播放完畢] 次數扣減成功！當前剩餘次數: $remainingCount")
        safeLog("TTS_DEBUG", "[$timestamp] [FIFO 播放完成扣減] 語音: '$TARGET_TEXT' / 剩餘次數: $remainingCount")

        // 歸零自動搬運 (屍體歸位)
        if (remainingCount <= 0) {
            val archiveTimestamp = dateFormat.format(Date())
            val record = RecycledAdRecord(
                archiveId = "ARCHIVE_HURLUNGCHUANG_${System.currentTimeMillis()}",
                adId = "AD_HURLUNGCHUANG_01",
                text = TARGET_TEXT,
                initialTotalCount = 100,
                finalRemainingCount = 0,
                archivedAt = archiveTimestamp
            )

            // 實體卡片檔案搬移至專屬回收/封存倉庫
            LocalCardDatabase.archiveCardFile("AD_HURLUNGCHUANG_01")
            BaichuanRepository.removeCard("AD_HURLUNGCHUANG_01")

            safeLog(
                "TTS_DEBUG",
                "[$archiveTimestamp] 📦 100 次倒數歸零！實體資料已自動封存搬移至專屬回收倉庫 (ArchiveID: ${record.archiveId})"
            )

            onArchived?.invoke(record)
        }

        return true
    }

    /**
     * 相容模擬介面 (無字數監聽，拉取 payload 後自動完成一次自然播放扣減流程)
     */
    fun simulateTtsTrigger(currentLat: Double, currentLng: Double, onArchived: (RecycledAdRecord) -> Unit) {
        val payload = checkAndPullPayload(currentLat, currentLng)
        if (payload != null) {
            onPlaybackCompleted(onArchived)
        }
    }

    /**
     * 重設測試狀態（供單元測試或實機重測使用）
     */
    fun resetTest() {
        remainingCount = 100
        safeLog("TTS_DEBUG", "[${getCurrentTimestamp()}] 廣濟宮聖王公 TTS 測試狀態已重設，剩餘次數: $remainingCount")
    }

    fun getRemainingCount(): Int = remainingCount
}
