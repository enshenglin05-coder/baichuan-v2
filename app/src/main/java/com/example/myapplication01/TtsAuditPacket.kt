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
     * 當卡片被觸動時執行扣次與歷程記錄
     */
    fun triggerTouch(lat: Double, lon: Double, content: String): Boolean {
        if (remainingQuota <= 0) {
            isCompleted = true
            return false
        }

        val currentSeq = totalQuota - remainingQuota + 1
        remainingQuota--

        val logEntry = TransmissionLogEntry(
            sequence = currentSeq,
            timestamp = System.currentTimeMillis(),
            latitude = lat,
            longitude = lon,
            speechContent = content
        )
        transmissionLogs.add(logEntry)

        if (remainingQuota <= 0) {
            isCompleted = true
        }
        return true
    }

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
