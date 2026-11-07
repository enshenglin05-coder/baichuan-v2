package com.example.myapplication01

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * 語音廣告模型 (TTS Ad Model)
 * 對應雲端/前端 TTSAdModel TypeScript 介面規格：
 * interface TTSAdModel {
 *   id: string;                  // 廣告識別碼
 *   text: string;                // 冠名廣告字串內容
 *   tokenCount: number;          // 依字數自動推算的算力/Token 數量
 *   location: {                  // 目標投放經緯度
 *     latitude: number;
 *     longitude: number;
 *   };
 *   totalCount: number;          // 預設發布總播放次數（例如：10000）
 *   remainingCount: number;      // 雲端目前剩餘播放次數
 *   status: 'ACTIVE' | 'ARCHIVED_ZERO' | 'PAUSED'; // 狀態：播報中 | 歸零封存 | 暫停
 *   createdAt: string;           // 建立時間 ISO timestamp
 * }
 */

enum class TTSAdStatus {
    @SerializedName("ACTIVE")
    ACTIVE,

    @SerializedName("ARCHIVED_ZERO")
    ARCHIVED_ZERO,

    @SerializedName("PAUSED")
    PAUSED,
}

data class TTSAdLocation(
    @SerializedName("latitude")
    val latitude: Double = 0.0,

    @SerializedName("longitude")
    val longitude: Double = 0.0,
)

data class TTSAdModel(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("text")
    val text: String = "",

    @SerializedName("tokenCount", alternate = ["token_count"])
    val tokenCount: Int = calculateTokenCount(text),

    @SerializedName("location")
    val location: TTSAdLocation = TTSAdLocation(),

    @SerializedName("totalCount", alternate = ["total_count"])
    val totalCount: Int = 10000,

    @SerializedName("remainingCount", alternate = ["remaining_count"])
    val remainingCount: Int = 10000,

    @SerializedName("status")
    val status: TTSAdStatus = TTSAdStatus.ACTIVE,

    @SerializedName("createdAt", alternate = ["created_at"])
    val createdAt: String = getCurrentIsoTimestamp(),
) {
    /**
     * 判斷目前廣告是否可以播放（狀態為 ACTIVE 且剩餘次數 > 0）
     */
    val isPlayable: Boolean
        get() = (status == TTSAdStatus.ACTIVE) && (remainingCount > 0)

    /**
     * 檢查此廣告投放地點是否在指定用戶距離 (公里) 範圍內
     */
    fun isWithinRadius(userLat: Double, userLng: Double, radiusKm: Double): Boolean {
        if ((location.latitude == 0.0) && (location.longitude == 0.0)) return true
        val dist = DistanceUtils.calculateDistance(userLat, userLng, location.latitude, location.longitude)
        return dist <= radiusKm
    }

    /**
     * 扣減剩餘播放次數；若歸零則自動標記狀態為 ARCHIVED_ZERO
     */
    fun consumePlayCount(): TTSAdModel {
        val newRemaining = (remainingCount - 1).coerceAtLeast(0)
        val newStatus = if (newRemaining == 0) TTSAdStatus.ARCHIVED_ZERO else status
        return copy(
            remainingCount = newRemaining,
            status = newStatus,
        )
    }

    companion object {
        /**
         * 依據字數自動推算 Token 算力消耗量
         * 中日韓 CJK 字元約 1 字 = 1.5 Tokens，英數標點約 1 字 = 0.5 Token
         */
        fun calculateTokenCount(text: String): Int {
            if (text.isBlank()) return 0
            var count = 0.0
            for (char in text) {
                val isCjk = (char.code in 0x4E00..0x9FFF) || (char.code in 0x3040..0x30FF) || (char.code in 0xAC00..0xD7AF)
                count += if (isCjk) 1.5 else 0.5
            }
            return count.toInt().coerceAtLeast(1)
        }

        /**
         * 產生標準 ISO 8601 Timestamp 字串
         */
        fun getCurrentIsoTimestamp(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }
    }
}
