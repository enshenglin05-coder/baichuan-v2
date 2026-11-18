package com.example.myapplication01

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * TTS 語音卡片倉庫目錄管理器 (TTS Repository Manager)
 * 實作規則：
 * 1. TTS 內容純化（僅播放文字卡片內容）：
 *    - 嚴格確保 TextToSpeech 在執行 speak() 時，只讀取純粹的文字內容 (JSON 中的 text 欄位值)。
 *    - 絕對禁止將經緯度、距離數值 ("距離 X.X 公里")、或除錯/狀態標籤 ("廣播封包 [ID]：") 拼接到語音播報內容中。
 * 2. 嚴格落實時間戳單一佇列排隊 (Strict FIFO by Timestamp)：
 *    - 區域篩選出多個封包後，依據 timestamp 進行由早至晚嚴格排序。
 *    - 每次只允許解鎖並播報時間戳最優先 (最早) 的【單一封包】。
 *    - 該封包計數倒數歸零 (remainingCount == 0) 並且推送回歸檔倉庫 (/archived/) 後，才能解鎖並輪到時間戳第二順位的封包。
 */
object TtsRepositoryManager {

    private var ttsRepositoryDirectory: File? = null
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private fun safeLogD(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun safeLogE(tag: String, msg: String, e: Throwable? = null) {
        try {
            android.util.Log.e(tag, msg, e)
        } catch (_: Throwable) {
            println("[$tag] $msg ${e?.message ?: ""}")
        }
    }

    data class TtsPacketRecord(
        @SerializedName("packetId", alternate = ["id", "cardId", "card_id"])
        val packetId: String = "TTS_PKT_${System.currentTimeMillis()}",

        @SerializedName("text", alternate = ["description", "speechContent", "content"])
        val text: String = "",

        @SerializedName("remainingCount", alternate = ["remaining_count", "remainingQuota", "remaining_quota"])
        val remainingCount: Int = 100,

        @SerializedName("timestamp", alternate = ["createdAt", "created_at", "time"])
        val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date()),

        @SerializedName("latitude", alternate = ["lat", "gps_lat"])
        val latitude: Double = 0.0,

        @SerializedName("longitude", alternate = ["lng", "gps_lng"])
        val longitude: Double = 0.0,

        @SerializedName("radiusKm", alternate = ["radius_km", "radius"])
        val radiusKm: Double = 6.0,

        @SerializedName("isArchived", alternate = ["is_archived", "archived"])
        val isArchived: Boolean = false
    ) {
        fun isPlayable(): Boolean = remainingCount > 0 && !isArchived

        fun parseTimestampMillis(): Long {
            return parseTimestampToMillis(timestamp)
        }

        /**
         * 取得純化文字內容（去除任何除錯或系統拼接標籤）
         */
        fun getPureText(): String {
            return text.trim()
        }
    }

    fun parseTimestampToMillis(timestampStr: String?): Long {
        if (timestampStr.isNullOrBlank()) return 0L
        timestampStr.toLongOrNull()?.let { return it }
        val dateFormats = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in dateFormats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                if (pattern.contains("'Z'")) {
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                }
                val d = sdf.parse(timestampStr)
                if (d != null) return d.time
            } catch (_: Exception) {}
        }
        return 0L
    }

    /**
     * 自動化檢測與初始化 TTS 語音卡片倉庫目錄 (/tts_repository_folder/)
     */
    fun ensureTtsRepositoryInitialized(context: Context? = null): File {
        ttsRepositoryDirectory?.let {
            if (it.exists()) return it
        }
        val baseDir = context?.applicationContext?.filesDir ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp", "tts_repo_temp")
        val ttsRepoDir = File(baseDir, "tts_repository_folder")
        if (!ttsRepoDir.exists()) {
            ttsRepoDir.mkdirs()
            safeLogD("TtsRepository", "📁 [自動化建置] 成功初始化專屬 TTS 語音卡片倉庫目錄: ${ttsRepoDir.absolutePath}")
        }
        ttsRepositoryDirectory = ttsRepoDir
        return ttsRepoDir
    }

    /**
     * 儲存或更新 TTS 封包 JSON 檔案至 /tts_repository_folder/
     */
    fun saveTtsPacket(
        packetId: String,
        text: String,
        remainingCount: Int = 100,
        context: Context? = null,
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        radiusKm: Double = 6.0,
        timestamp: String? = null
    ): File? {
        return try {
            val dir = ensureTtsRepositoryInitialized(context)
            val ts = if (!timestamp.isNullOrBlank()) timestamp else SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val record = TtsPacketRecord(
                packetId = packetId,
                text = text.trim(),
                remainingCount = remainingCount,
                timestamp = ts,
                latitude = latitude,
                longitude = longitude,
                radiusKm = radiusKm
            )
            val file = File(dir, "$packetId.json")
            val json = gson.toJson(record)
            file.writeText(json, Charsets.UTF_8)
            safeLogD("TtsRepository", "📢 [TTS 封包建立/更新] PacketID: [$packetId] | Audit Timestamp: [$ts] | 純化內容: \"${record.getPureText()}\" | 剩餘次數: $remainingCount")

            try {
                BaichuanApiService.publishTtsPacketToHub(packetId, json)
            } catch (e: Exception) {
                safeLogE("TtsRepository", "❌ [TTS 同步失敗] packetId=$packetId: ${e.message}")
            }
            file
        } catch (e: Exception) {
            safeLogE("TtsRepository", "❌ 寫入 TTS 封包失敗: ${e.message}", e)
            null
        }
    }

    /**
     * 載入 /tts_repository_folder/ 中所有本地 TTS 封包記錄
     */
    fun loadAllTtsPackets(context: Context? = null): List<TtsPacketRecord> {
        val dir = ensureTtsRepositoryInitialized(context)
        val files = dir.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) } ?: return emptyList()
        val list = mutableListOf<TtsPacketRecord>()
        for (file in files) {
            try {
                val json = file.readText(Charsets.UTF_8)
                val record = gson.fromJson(json, TtsPacketRecord::class.java)
                if (record != null && record.packetId.isNotBlank()) {
                    list.add(record)
                }
            } catch (_: Exception) {}
        }
        return list.sortedBy { it.parseTimestampMillis() }
    }

    /**
     * 刪除指定的 TTS 封包檔案
     */
    fun deleteTtsPacket(packetId: String, context: Context? = null): Boolean {
        return try {
            val dir = ensureTtsRepositoryInitialized(context)
            val file = File(dir, "$packetId.json")
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 嚴格落實時間戳單一佇列排隊 (Strict FIFO by Timestamp)
     *
     * 1. 區域篩選出多個封包後，依據 timestamp 進行由早至晚嚴格排序。
     * 2. 每次只允許解鎖並返回時間戳最優先 (最早) 的【單一封包】。
     * 3. 時間戳較後順位之封包維持鎖定狀態，直到優先封包次數倒數歸零 (remainingCount == 0) 並完成推回歸檔區後，始解鎖輪到下一順位。
     */
    fun fetchStrictFifoSinglePacket(
        context: Context?,
        userLat: Double?,
        userLng: Double?,
        radiusKm: Double = 6.0,
        onResult: (topPriorityPacket: TtsPacketRecord?, allQualifiedPackets: List<TtsPacketRecord>) -> Unit
    ) {
        val ctx = context?.applicationContext
        val effectiveLat = userLat ?: GlobalState.globalLatitude ?: LocationFlowManager.currentLocation?.first ?: HurlungchuangTestManager.TARGET_LATITUDE
        val effectiveLng = userLng ?: GlobalState.globalLongitude ?: LocationFlowManager.currentLocation?.second ?: HurlungchuangTestManager.TARGET_LONGITUDE

        BaichuanApiService.fetchTtsPacketsFromRemoteHub { remotePackets ->
            val localPackets = loadAllTtsPackets(ctx)
            val archiveDir = File(ensureTtsRepositoryInitialized(ctx), "archived")
            val localArchivedIds = archiveDir.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) }
                ?.map { it.name.removeSuffix(".json") }?.toSet() ?: emptySet()

            val packetMap = mutableMapOf<String, TtsPacketRecord>()
            for (p in localPackets) {
                if (p.packetId.isNotBlank()) packetMap[p.packetId] = p
            }
            for (p in remotePackets) {
                if (p.packetId.isNotBlank()) packetMap[p.packetId] = p
            }

            val allPackets = packetMap.values.toList()

            safeLogD("TtsRepository", "================================================================================")
            safeLogD("TtsRepository", "🔍 [TTS 嚴格時間戳單一佇列排隊 (Strict FIFO by Timestamp)]")
            safeLogD("TtsRepository", "• 當前 GPS 座標: ($effectiveLat, $effectiveLng) | 設定搜尋半徑: $radiusKm km")
            safeLogD("TtsRepository", "• 倉庫拉取封包總筆數: ${allPackets.size} 筆 (遠端: ${remotePackets.size}, 本地: ${localPackets.size}, 歸檔區: ${localArchivedIds.size})")
            safeLogD("TtsRepository", "--------------------------------------------------------------------------------")

            val qualifiedPackets = mutableListOf<Pair<TtsPacketRecord, Double>>()

            for (packet in allPackets) {
                if (packet.isArchived || packet.remainingCount <= 0 || localArchivedIds.contains(packet.packetId)) {
                    safeLogD("TtsRepository", "  - 封包 [${packet.packetId}]: 已歸零/已歸檔 ➔ ❌ 略過")
                    continue
                }

                val pLat = if (packet.latitude != 0.0) packet.latitude else effectiveLat
                val pLng = if (packet.longitude != 0.0) packet.longitude else effectiveLng
                val dist = DistanceUtils.calculateDistance(effectiveLat, effectiveLng, pLat, pLng)
                val maxAllowedRadius = if (packet.radiusKm > 0) packet.radiusKm else radiusKm

                val isMatched = dist <= maxAllowedRadius && dist <= radiusKm
                safeLogD("TtsRepository", "  - 封包 [${packet.packetId}]: 距離 ${String.format(Locale.getDefault(), "%.2f", dist)} km | 剩餘次數: ${packet.remainingCount} | Audit Timestamp: [${packet.timestamp}] ➔ ${if (isMatched) "✅ 符合區域範圍" else "❌ 超出範圍"}")

                if (isMatched) {
                    qualifiedPackets.add(Pair(packet, dist))
                }
            }

            // 依據 timestamp 進行由早至晚 (earliest first) 嚴格排序
            val sortedQualified = qualifiedPackets.sortedBy { (packet, _) -> packet.parseTimestampMillis() }
            val sortedPackets = sortedQualified.map { it.first }

            safeLogD("TtsRepository", "--------------------------------------------------------------------------------")
            if (sortedPackets.isEmpty()) {
                safeLogD("TtsRepository", "⚠️ 當前涵蓋範圍內無合格 TTS 廣播封包，維護靜默狀態。")
                safeLogD("TtsRepository", "================================================================================")
                onResult(null, emptyList())
                return@fetchTtsPacketsFromRemoteHub
            }

            // 每次只允許解鎖並播報【時間戳最優先】的第一個封包
            val topPriorityPacket = sortedPackets.first()

            safeLogD("TtsRepository", "🎯 🔓 [單一佇列鎖定 - 時間戳最優先封包 (Priority #1)]")
            safeLogD("TtsRepository", "• 播報目標: PacketID [${topPriorityPacket.packetId}] | Timestamp: [${topPriorityPacket.timestamp}] | 剩餘次數: ${topPriorityPacket.remainingCount}")
            safeLogD("TtsRepository", "• 純化播報文字 (純內容無雜質): \"${topPriorityPacket.getPureText()}\"")

            if (sortedPackets.size > 1) {
                safeLogD("TtsRepository", "🔒 [其餘封包佇列鎖定中] 以下 ${sortedPackets.size - 1} 筆較晚時間戳封包保持等待，直到 [${topPriorityPacket.packetId}] 倒數歸零歸檔後始解鎖:")
                sortedPackets.drop(1).forEachIndexed { index, p ->
                    safeLogD("TtsRepository", "  - 等待順位 #${index + 2}: PacketID [${p.packetId}] | Timestamp: [${p.timestamp}] | 剩餘: ${p.remainingCount} 次 | 純文字: \"${p.getPureText()}\"")
                }
            }
            safeLogD("TtsRepository", "================================================================================")

            onResult(topPriorityPacket, sortedPackets)
        }
    }

    /**
     * 全量拉取並依據 Strict FIFO by Timestamp 傳回合格封包列表
     */
    fun fetchAllAndPerformMultiPacketGeoMatching(
        context: Context?,
        userLat: Double?,
        userLng: Double?,
        radiusKm: Double = 6.0,
        onResult: (List<TtsPacketRecord>) -> Unit
    ) {
        fetchStrictFifoSinglePacket(context, userLat, userLng, radiusKm) { _, sortedPackets ->
            onResult(sortedPackets)
        }
    }

    /**
     * 播放次數即時倒數 (-1) 與歸零自動回推 (Countdown & Archive Push)
     *
     * 當封包播報完畢 1 次時：
     * 1. 將該封包的 remainingCount 進行 -1 倒數，並更新精確時間戳 (Audit Timestamp)。
     * 2. 若 remainingCount > 0：即時更新本地與遠端活躍倉庫 (/tts_repository_folder/)。
     * 3. 若 remainingCount == 0：自動觸發背景非同步寫入，將封包從當前活躍倉庫 (Active Folder) 移出刪除，
     *    並推回至遠端歸檔倉庫 (/archived/)，確保活躍列表中不留空值，同時【解鎖時間戳第二順位之封包】。
     */
    fun consumePacketPlaybackCount(
        packetId: String,
        context: Context? = null,
        onComplete: ((TtsPacketRecord?, Boolean) -> Unit)? = null
    ) {
        val dir = ensureTtsRepositoryInitialized(context)
        val file = File(dir, "$packetId.json")

        var record: TtsPacketRecord? = null
        if (file.exists()) {
            try {
                val json = file.readText(Charsets.UTF_8)
                record = gson.fromJson(json, TtsPacketRecord::class.java)
            } catch (_: Exception) {}
        }
        if (record == null) {
            val all = loadAllTtsPackets(context)
            record = all.find { it.packetId == packetId }
        }

        val targetRecord = record ?: TtsPacketRecord(packetId = packetId, text = "", remainingCount = 100)
        val nowTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val updatedCount = (targetRecord.remainingCount - 1).coerceAtLeast(0)
        val updatedRecord = targetRecord.copy(
            remainingCount = updatedCount,
            timestamp = nowTimestamp,
            isArchived = updatedCount <= 0
        )

        val jsonOutput = gson.toJson(updatedRecord)

        if (updatedCount > 0) {
            // 次數 > 0：即時更新活躍倉庫檔
            file.writeText(jsonOutput, Charsets.UTF_8)
            safeLogD("TtsRepository", "📉 [TTS 播放即時倒數 -1] PacketID: [$packetId] 扣減完畢 1 次 ➔ 新剩餘次數: $updatedCount | Audit Timestamp: [$nowTimestamp] | 純化內容: \"${updatedRecord.getPureText()}\"")
            BaichuanApiService.publishTtsPacketToHub(packetId, jsonOutput)
            onComplete?.invoke(updatedRecord, false)
        } else {
            // 次數 == 0：倒數歸零自動回推 (從 Active Folder 移出推回 Archive，解鎖下一順位)
            if (file.exists()) file.delete()

            val archiveDir = File(dir, "archived")
            if (!archiveDir.exists()) archiveDir.mkdirs()
            val archiveFile = File(archiveDir, "$packetId.json")
            archiveFile.writeText(jsonOutput, Charsets.UTF_8)

            safeLogD("TtsRepository", "🏁 🔓 [倒數歸零自動回推與解鎖] PacketID: [$packetId] 剩餘次數歸零 (remainingCount=0)，已從活躍倉庫移出，推回歸檔倉庫 (/archived/) ➔ 即刻解鎖時間戳第二順位之封包！ | Audit Timestamp: [$nowTimestamp]")

            BaichuanApiService.deleteActiveTtsPacketAndPushToArchive(packetId, jsonOutput, {
                safeLogD("TtsRepository", "✅ [歸檔推回與活躍區清理成功] PacketID: [$packetId] 活躍檔已移出，解鎖佇列下一順位。")
                onComplete?.invoke(updatedRecord, true)
            }, { err ->
                safeLogE("TtsRepository", "⚠️ [歸檔推回回應] PacketID: [$packetId]: $err")
                onComplete?.invoke(updatedRecord, true)
            })
        }
    }

    /**
     * 同步版本：將指定的 TTS 封包歸零並移轉至歸檔倉庫
     */
    fun zeroOutAndArchive(packetId: String, context: Context? = null) {
        zeroOutAndArchiveAsync(packetId, context)
    }

    /**
     * 當佇列中的某一筆封包播報完畢、次數歸零後，自動觸發背景非同步歸檔 (推回歸檔區)
     */
    fun zeroOutAndArchiveAsync(packetId: String, context: Context? = null, onArchived: (() -> Unit)? = null) {
        consumePacketPlaybackCount(packetId, context) { _, _ ->
            onArchived?.invoke()
        }
    }
}
