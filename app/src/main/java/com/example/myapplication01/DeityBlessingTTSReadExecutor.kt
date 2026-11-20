package com.example.myapplication01

import android.content.Context
import android.util.Log
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * DeityBlessingTTSReadExecutor：獨立的 TTS Repository 唯讀執行程式碼
 *
 * 功能目標：
 * 1. 統一執行 TTS 讀取任務，具備獨立的任務 UUID (僅作任務識別碼，絕不取代 packetId)。
 * 2. 嚴格唯讀：僅負責讀取遠端/本地 TTS 倉庫、JSON 解析、GPS 與半徑篩選、7位數編號總合值與 FIFO 排序，絕對禁止任何寫入、PUT、扣次數、刪除或歸檔行為。
 * 3. 並發控制：任務隔離、執行狀態追蹤，防止舊任務覆蓋新任務或結果混淆。
 * 4. 錯誤防禦：讀取失敗時回傳空結果，絕不建立空封包或預設封包。
 */
object DeityBlessingTTSReadExecutor {

    private const val TAG = "DeityReadExecutor"

    private val activeReadTasks = ConcurrentHashMap<String, Long>()

    /**
     * 執行一次獨立的唯讀 TTS 讀取任務
     * @param context Context
     * @param userLat 使用者緯度
     * @param userLng 使用者經度
     * @param radiusKm 搜尋半徑 (預設 6.0 公里)
     * @param onResult 回調 (taskId, topPriorityPacket, allQualifiedPackets)
     */
    fun executeReadTask(
        context: Context?,
        userLat: Double?,
        userLng: Double?,
        radiusKm: Double = 6.0,
        onResult: (taskId: String, topPacket: TtsRepositoryManager.TtsPacketRecord?, allQualified: List<TtsRepositoryManager.TtsPacketRecord>) -> Unit
    ) {
        val taskId = "read_task_${UUID.randomUUID().toString()}"
        val startTime = System.currentTimeMillis()
        activeReadTasks[taskId] = startTime

        Log.d(TAG, "🚀 [TTS 唯讀讀取任務開始] TaskUUID: $taskId | Lat: $userLat, Lng: $userLng, Radius: $radiusKm km")

        // 嚴格唯讀呼叫：委託 TtsRepositoryManager 進行純讀取、GPS 篩選、7位數總合值排序與 FIFO 檢索
        TtsRepositoryManager.fetchStrictFifoSinglePacket(context, userLat, userLng, radiusKm) { topPacket, sortedPackets ->
            val duration = System.currentTimeMillis() - startTime
            activeReadTasks.remove(taskId)

            Log.d(TAG, "🏁 [TTS 唯讀讀取任務完成] TaskUUID: $taskId | 耗時: ${duration}ms | 優先封包: ${topPacket?.packetId ?: "null"} | 合格總數: ${sortedPackets.size}")

            // 嚴格綁定 TaskID 與回調結果，避免不同任務污染
            try {
                onResult(taskId, topPacket, sortedPackets)
            } catch (e: Exception) {
                Log.e(TAG, "❌ [TaskUUID: $taskId] 讀取結果回調執行異常: ${e.message}", e)
            }
        }
    }
}
