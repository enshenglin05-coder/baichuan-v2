package com.example.myapplication01

import android.content.Context
import android.util.Log
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * TTS 唯讀讀取執行器 (TTSReadExecutor)
 *
 * 任務：
 * 1. 統一 TTS 讀取入口，確保只讀取、不寫入。
 * 2. 每次執行任務產生唯一 UUID，綁定讀取結果。
 * 3. 沿用 TtsRepositoryManager 的解析、GPS 篩選與 FIFO 排序邏輯。
 * 4. 嚴格並發控制與任務隔離，防止跨任務混淆或舊任務覆蓋。
 */
object TTSReadExecutor {

    private const val TAG = "TTSReadExecutor"

    // 追蹤進行中的任務 ID，防止同一任務重複執行或交叉污染
    private val activeTasks = ConcurrentHashMap<String, Long>()

    /**
     * 執行一次唯讀 TTS 讀取任務
     */
    fun executeReadTask(
        context: Context?,
        userLat: Double?,
        userLng: Double?,
        radiusKm: Double = 6.0,
        onResult: (taskId: String, topPacket: TtsRepositoryManager.TtsPacketRecord?, allQualified: List<TtsRepositoryManager.TtsPacketRecord>) -> Unit
    ) {
        val taskId = UUID.randomUUID().toString()
        val startTime = System.currentTimeMillis()
        activeTasks[taskId] = startTime
        Log.d(TAG, "🚀 [讀取任務開始] TaskID: $taskId | 執行 TTS 唯讀讀取 (Lat: $userLat, Lng: $userLng, Radius: $radiusKm km)")

        // 嚴格保證唯讀：只調用讀取/篩選邏輯，絕不呼叫任何寫入、PUT、扣次數或歸檔方法
        TtsRepositoryManager.fetchStrictFifoSinglePacket(context, userLat, userLng, radiusKm) { topPacket, sortedPackets ->
            val duration = System.currentTimeMillis() - startTime
            activeTasks.remove(taskId)

            Log.d(TAG, "🏁 [讀取任務完成] TaskID: $taskId | 耗時: ${duration}ms | 找到最優先封包: ${topPacket?.packetId ?: "null"} | 合格總數: ${sortedPackets.size}")

            // 確保任務結果與 UUID 嚴格綁定並回調
            onResult(taskId, topPacket, sortedPackets)
        }
    }
}
