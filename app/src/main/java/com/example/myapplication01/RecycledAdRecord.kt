package com.example.myapplication01

import com.google.gson.annotations.SerializedName
import java.util.UUID

/**
 * 廣告播放足跡紀錄項
 */
data class RecycledAdLog(
    @SerializedName("timestamp")
    val timestamp: String = TTSAdModel.getCurrentIsoTimestamp(),

    @SerializedName("triggerCount", alternate = ["trigger_count"])
    val triggerCount: Int = 1,
)

/**
 * 廣告歸零回收紀錄模型 (Recycled Ad Record)
 * 對應雲端/前端 RecycledAdRecord TypeScript 介面規格：
 * interface RecycledAdRecord {
 *   archiveId: string;           // 回收紀錄編號
 *   adId: string;                // 原廣告 ID
 *   text: string;                // 播報字串
 *   location: {                  // 原始經緯度
 *     latitude: number;
 *     longitude: number;
 *   };
 *   initialTotalCount: number;   // 原始購買次數
 *   finalRemainingCount: number; // 歸零確認 (應為 0)
 *   archivedAt: string;          // 歸零自動回傳時間
 *   logs: Array<{                // 播放履約歷史足跡 (佐證用)
 *     timestamp: string;
 *     triggerCount: number;
 *   }>;
 * }
 */
data class RecycledAdRecord(
    @SerializedName("archiveId", alternate = ["archive_id"])
    val archiveId: String = "arc_${UUID.randomUUID().toString().replace("-", "").take(8)}",

    @SerializedName("adId", alternate = ["ad_id"])
    val adId: String = "",

    @SerializedName("text")
    val text: String = "",

    @SerializedName("location")
    val location: TTSAdLocation = TTSAdLocation(),

    @SerializedName("initialTotalCount", alternate = ["initial_total_count"])
    val initialTotalCount: Int = 0,

    @SerializedName("finalRemainingCount", alternate = ["final_remaining_count"])
    val finalRemainingCount: Int = 0,

    @SerializedName("archivedAt", alternate = ["archived_at"])
    val archivedAt: String = TTSAdModel.getCurrentIsoTimestamp(),

    @SerializedName("logs")
    val logs: List<RecycledAdLog> = emptyList(),
) {
    companion object {
        /**
         * 由 TTSAdModel 與履約足跡轉換產生 RecycledAdRecord
         */
        fun fromAdModel(adModel: TTSAdModel, logs: List<RecycledAdLog> = emptyList()): RecycledAdRecord {
            return RecycledAdRecord(
                archiveId = "arc_${UUID.randomUUID().toString().replace("-", "").take(8)}",
                adId = adModel.id,
                text = adModel.text,
                location = adModel.location,
                initialTotalCount = adModel.totalCount,
                finalRemainingCount = adModel.remainingCount,
                archivedAt = TTSAdModel.getCurrentIsoTimestamp(),
                logs = logs,
            )
        }
    }
}
