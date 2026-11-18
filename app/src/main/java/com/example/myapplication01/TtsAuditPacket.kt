package com.example.myapplication01

import org.json.JSONArray
import org.json.JSONObject

/**
 * 實體執行：100 次計次與時間戳憑證封包模型
 * 職責：記錄每次觸動的時間、座標與剩餘次數，作為後端無可辯駁的鐵證。
 */
data class TransmissionLogEntry(
    val sequence: Int,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speechContent: String
)

data class TtsAuditPacket(
    val cardId: String,
    val senderId: String,
    val totalQuota: Int = 100,
    var remainingQuota: Int,
    val transmissionLogs: MutableList<TransmissionLogEntry> = mutableListOf(),
    var isCompleted: Boolean = false
) {



    /**
     * 轉為 JSON 封包格式，直接寫入遠端倉庫的 TTS 資料夾中
     */
    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("cardId", cardId)
        json.put("senderId", senderId)
        json.put("totalQuota", totalQuota)
        json.put("remainingQuota", remainingQuota)
        json.put("isCompleted", isCompleted)

        val logsArray = JSONArray()
        for (log in transmissionLogs) {
            val logObj = JSONObject().apply {
                put("sequence", log.sequence)
                put("timestamp", log.timestamp)
                put("latitude", log.latitude)
                put("longitude", log.longitude)
                put("speechContent", log.speechContent)
            }
            logsArray.put(logObj)
        }
        json.put("transmissionLogs", logsArray)
        return json
    }
}
