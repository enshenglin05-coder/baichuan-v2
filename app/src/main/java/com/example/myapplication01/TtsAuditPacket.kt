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
     * 觸動一次 TTS 語音播報並紀錄時間戳憑證與地點資訊
     */
    fun triggerTouch(latitude: Double, longitude: Double, speechContent: String): Boolean {
        if (remainingQuota <= 0 || isCompleted) {
            isCompleted = true
            return false
        }

        // 印出當前呼叫堆疊與來源路徑，精準追查封包來源
        try {
            val sourceTrace = Thread.currentThread().stackTrace
            val callerInfo = sourceTrace.getOrNull(3)?.let { 
                "File: ${it.fileName}, Method: ${it.methodName}, Line: ${it.lineNumber}" 
            } ?: "Unknown Source"
            android.util.Log.d("PacketSourceTracker", "=== 攔截到 TTS 封包觸動 ===")
            android.util.Log.d("PacketSourceTracker", "來源路徑詳情 -> $callerInfo")
        } catch (_: Throwable) {
            // JVM / Standard Unit Test Fallback
        }

        remainingQuota -= 1
        val seq = transmissionLogs.size + 1
        val entry = TransmissionLogEntry(
            sequence = seq,
            timestamp = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude,
            speechContent = speechContent
        )
        transmissionLogs.add(entry)

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
