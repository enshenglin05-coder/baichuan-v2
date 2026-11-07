package com.example.myapplication01

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 烘爐地土地公 TTS 倒數與範圍觸發測試管理器
 */
object HurlungchuangTestManager {

    // 1. 目標位置設定：25°00'09.1"N 121°29'51.1"E (25.002528, 121.497528)
    const val TARGET_LATITUDE = 25.002528
    const val TARGET_LONGITUDE = 121.497528
    
    // 範圍判定 (10 公里內)
    const val RADIUS_KM = 10.0

    // 2. 播報內容
    const val TARGET_TEXT = "新北市中和區廣濟宮聖王公守護你"

    // 3. 次數倒數與歸零設定 (100次播放，100個封包，觸動一次拉出一個封包並扣除)
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
     * 執行位置計算與 10 公里範圍判定
     */
    fun checkLocationAndDistance(currentLat: Double, currentLng: Double): Boolean {
        val timestamp = getCurrentTimestamp()
        val distance = DistanceUtils.calculateDistance(TARGET_LATITUDE, TARGET_LONGITUDE, currentLat, currentLng)
        val isWithinRange = distance <= RADIUS_KM

        safeLog(
            "TTS_DEBUG",
            "[$timestamp] 位置計算與距離判斷 - 目標經緯度: ($TARGET_LATITUDE, $TARGET_LONGITUDE), 當前經緯度: ($currentLat, $currentLng), 計算距離: ${String.format(Locale.getDefault(), "%.3f", distance)} 公里, 是否在 10 公里內: $isWithinRange, 當前剩餘次數: $remainingCount"
        )

        return isWithinRange
    }

    /**
     * 執行 TTS 倒數第二個字精準捕捉、一次拉取與計時器扣減機制
     */
    fun simulateTtsTrigger(currentLat: Double, currentLng: Double, onArchived: (RecycledAdRecord) -> Unit) {
        val timestamp = getCurrentTimestamp()

        // 每次執行位置計算與 10 公里範圍判定
        val isWithin = checkLocationAndDistance(currentLat, currentLng)

        // 檢查距離
        if (!isWithin) {
            val distance = DistanceUtils.calculateDistance(TARGET_LATITUDE, TARGET_LONGITUDE, currentLat, currentLng)
            safeLog("TTS_DEBUG", "[$timestamp] 距離超過 10 公里範圍，不觸發 TTS 倒數與倒數第二字機制。目標經緯度: ($TARGET_LATITUDE, $TARGET_LONGITUDE), 當前經緯度: ($currentLat, $currentLng), 計算距離: ${String.format(Locale.getDefault(), "%.3f", distance)} 公里, 剩餘次數: $remainingCount")
            return
        }

        if (remainingCount <= 0) {
            safeLog("TTS_DEBUG", "[$timestamp] 剩餘次數已歸零，不再進行倒數。當前剩餘次數: $remainingCount")
            return
        }

        // 精準抓取倒數第二個字
        // 文字: "中和烘爐地，南山土地公，福氣送給你"
        val secondToLastChar = if (TARGET_TEXT.length >= 2) TARGET_TEXT[TARGET_TEXT.length - 2] else ' '

        remainingCount--

        // 倒數第二個字精準觸發與次數扣減日誌
        safeLog("CARD_COUNT", "🔥 倒數第二字觸發成功！當前剩餘次數: " + remainingCount)
        safeLog(
            "TTS_DEBUG",
            "[TTS_DEBUG] 鎖定倒數第二個字精準觸發成功：時間 [$timestamp] / 剩餘次數 [$remainingCount] / 倒數第二字: '$secondToLastChar' / 播報文字: '$TARGET_TEXT'"
        )

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

            // 實體卡片檔案搬移至後端專屬回收/封存倉庫
            LocalCardDatabase.archiveCardFile("AD_HURLUNGCHUANG_01")
            BaichuanRepository.removeCard("AD_HURLUNGCHUANG_01")

            safeLog(
                "TTS_DEBUG",
                "[$archiveTimestamp] 100 次倒數歸零！實體資料已搬移並打包推回專屬回收/封存倉庫。 ArchiveID: ${record.archiveId}, 封存時間: $archiveTimestamp"
            )

            onArchived(record)
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
