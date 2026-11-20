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

    private fun safeLogW(tag: String, msg: String) {
        try {
            android.util.Log.w(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    /**
     * 計算 TTS 封包 / 檔案夾上 7 位數編號的各個位數總合值 (Sum of Digits 1 to 7)
     * 例如："1234567" -> 1+2+3+4+5+6+7 = 28
     * 比對規則：當經緯度與外送/推送距離比對符合，區域內有多筆 TTS 封包重疊時，
     * 讀取 UI 5 上面的 7 位數封包/檔案夾編號，計算 1 到 7 位數總合值。
     * 總合值最小者最優先拉取與播放（總合值小的排前面，大的排後面）。
     */
    fun calculatePacket7DigitSum(packetId: String, packetCode: String? = null): Int {
        val targetStr = when {
            !packetCode.isNullOrBlank() -> packetCode
            packetId.contains("ui5_pkt_") -> packetId.substringAfter("ui5_pkt_").substringBefore("_")
            else -> packetId
        }
        val digits = targetStr.filter { it.isDigit() }
        if (digits.isEmpty()) return 999
        val first7Digits = if (digits.length >= 7) digits.substring(0, 7) else digits
        return first7Digits.sumOf { it.digitToInt() }
    }

    data class TtsPacketRecord(
        @SerializedName("packetId", alternate = ["id", "cardId", "card_id"])
        val packetId: String = "TTS_PKT_${System.currentTimeMillis()}",

        @SerializedName("packetCode", alternate = ["packet_code", "folderCode", "folder_code", "code7"])
        val packetCode: String = "",

        @SerializedName("text", alternate = ["description", "speechContent", "content"])
        val text: String = "",

        @SerializedName("remainingCount", alternate = ["remaining_count", "remainingQuota", "remaining_quota"])
        val remainingCount: Int = 100,

        @SerializedName("timestamp", alternate = ["createdAt", "created_at", "time"])
        val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date()),

        @SerializedName("latitude", alternate = ["lat", "gps_lat"])
        val latitude: Double? = null,

        @SerializedName("longitude", alternate = ["lng", "gps_lng"])
        val longitude: Double? = null,

        @SerializedName("radiusKm", alternate = ["radius_km", "radius"])
        val radiusKm: Double? = null,

        @SerializedName("formulaIdentifier", alternate = ["formula_identifier", "formulaId", "formula_id"])
        val formulaIdentifier: String = DistanceUtils.FORMULA_ID,

        @SerializedName("formulaVersion", alternate = ["formula_version", "formulaVer", "formula_ver"])
        val formulaVersion: String = DistanceUtils.FORMULA_VERSION,

        @SerializedName("geofenceType", alternate = ["geofence_type", "geofence"])
        val geofenceType: String = "RADIAL_CIRCLE",

        @SerializedName("geofenceParams", alternate = ["geofence_params", "geofence_parameters"])
        val geofenceParams: Map<String, Any>? = null,

        @SerializedName("isArchived", alternate = ["is_archived", "archived"])
        val isArchived: Boolean = false
    ) {
        fun isPlayable(): Boolean = remainingCount > 0 && !isArchived

        fun parseTimestampMillis(): Long {
            return parseTimestampToMillis(timestamp)
        }

        fun getDigitSum(): Int {
            return calculatePacket7DigitSum(packetId, packetCode)
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
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double? = null,
        timestamp: String? = null,
        packetCode: String? = null,
        formulaIdentifier: String = DistanceUtils.FORMULA_ID,
        formulaVersion: String = DistanceUtils.FORMULA_VERSION,
        geofenceType: String = "RADIAL_CIRCLE",
        geofenceParams: Map<String, Any>? = null
    ): File? {
        return try {
            // 印出當前呼叫堆疊與來源路徑，精準追查封包來源
            try {
                val sourceTrace = Thread.currentThread().stackTrace
                val callerInfo = sourceTrace.getOrNull(3)?.let { 
                    "File: ${it.fileName}, Method: ${it.methodName}, Line: ${it.lineNumber}" 
                } ?: "Unknown Source"
                safeLogD("PacketSourceTracker", "=== 攔截到 TTS 封包寫入 (TtsRepositoryManager) ===")
                safeLogD("PacketSourceTracker", "來源路徑詳情 -> $callerInfo")
            } catch (_: Throwable) {
                // Fallback for non-Android unit test runtimes
            }

            if (latitude == null || longitude == null || latitude.isNaN() || longitude.isNaN()) {
                safeLogE("TtsRepository", "❌ [拒絕寫入] 缺失或無效之 GPS 座標，不予產生虛構座標。packetId=$packetId")
                return null
            }

            val dir = ensureTtsRepositoryInitialized(context)
            if (remainingCount <= 0) {
                deleteTtsPacket(packetId, context)
                safeLogD("TtsRepository", "🗑️ [次數歸零/直接拋棄] PacketID: [$packetId] 剩餘次數歸零 ($remainingCount)，直接拋棄刪除，不做儲存動作。")
                try {
                    val zeroRecord = TtsPacketRecord(packetId = packetId, packetCode = packetCode ?: "", text = text.trim(), remainingCount = 0, isArchived = true)
                    BaichuanApiService.deleteActiveTtsPacketAndPushToArchive(packetId, gson.toJson(zeroRecord))
                } catch (e: Exception) {
                    safeLogE("TtsRepository", "❌ [TTS 後端取消推送失敗] packetId=$packetId: ${e.message}")
                }
                return null
            }

            val ts = if (!timestamp.isNullOrBlank()) timestamp else SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val effectiveRadius = if (radiusKm != null && radiusKm > 0.0) radiusKm else 6.0

            val record = TtsPacketRecord(
                packetId = packetId,
                packetCode = packetCode ?: "",
                text = text.trim(),
                remainingCount = remainingCount,
                timestamp = ts,
                latitude = latitude,
                longitude = longitude,
                radiusKm = effectiveRadius,
                formulaIdentifier = formulaIdentifier,
                formulaVersion = formulaVersion,
                geofenceType = geofenceType,
                geofenceParams = geofenceParams ?: mapOf(
                    "centerLat" to latitude,
                    "centerLng" to longitude,
                    "radiusKm" to effectiveRadius
                )
            )
            val file = File(dir, "$packetId.json")
            val json = gson.toJson(record)
            file.writeText(json, Charsets.UTF_8)
            safeLogD("TtsRepository", "📢 [TTS 封包建立/更新] PacketID: [$packetId] | 7位數編號: [${record.packetCode}] | 總合值: ${record.getDigitSum()} | Audit Timestamp: [$ts] | 純化內容: \"${record.getPureText()}\" | 剩餘次數: $remainingCount")

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

    fun isBannedTestPacketId(packetId: String): Boolean {
        val lower = packetId.lowercase()
        return lower.contains("broadcast_delivery") ||
               lower.contains("packet_earlier") ||
               lower.contains("packet_later") ||
               lower.contains("pure_pkt_priority") ||
               lower.contains("test_ide_master") ||
               lower.contains("tts_packet_test") ||
               lower.contains("packet_countdown_audit") ||
               lower.startsWith("test_")
    }

    fun parseTtsPacketFromJson(json: String, fallbackId: String = ""): TtsPacketRecord? {
        return try {
            val record = gson.fromJson(json, TtsPacketRecord::class.java) ?: return null
            val resolvedId = if (record.packetId.isNotBlank()) record.packetId else fallbackId
            if (isBannedTestPacketId(resolvedId)) {
                safeLogW("TtsRepository", "⛔ [最高禁令] 攔截並丟棄違規測試/廣播封包: $resolvedId")
                return null
            }
            if (record.remainingCount <= 0 || record.isArchived) {
                safeLogW("TtsRepository", "🗑️ [歸零/已廢棄封包] 發現剩餘次數 <= 0 或已歸檔封包 ($resolvedId)，直接拋棄丟掉，不再記憶。")
                return null
            }
            var resolvedText = record.text
            if (resolvedText.isBlank()) {
                try {
                    val jsonObj = com.google.gson.JsonParser.parseString(json).asJsonObject
                    for (key in arrayOf("text", "description", "speechContent", "content", "desc", "message", "speech", "speechText", "speech_text", "card_text", "body", "title")) {
                        if (jsonObj.has(key) && !jsonObj.get(key).isJsonNull) {
                            val valStr = jsonObj.get(key).asString
                            if (valStr.isNotBlank()) {
                                resolvedText = valStr
                                break
                            }
                        }
                    }
                } catch (_: Exception) {}
            }
            record.copy(packetId = resolvedId, text = resolvedText)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 載入 /tts_repository_folder/ 中所有本地 TTS 封包記錄 (自動依 7 位數編號總合值小者優先排序)
     */
    fun loadAllTtsPackets(context: Context? = null): List<TtsPacketRecord> {
        val dir = ensureTtsRepositoryInitialized(context)
        val files = dir.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) } ?: return emptyList()
        val list = mutableListOf<TtsPacketRecord>()
        for (file in files) {
            try {
                val json = file.readText(Charsets.UTF_8)
                val record = parseTtsPacketFromJson(json, file.name.removeSuffix(".json"))
                if (record != null && record.packetId.isNotBlank() && !isBannedTestPacketId(record.packetId) && record.remainingCount > 0 && !record.isArchived) {
                    list.add(record)
                } else {
                    file.delete()
                    safeLogD("TtsRepository", "🗑️ [本地即時清理] 徹底刪除無效/歸零/歸檔檔案: ${file.name}")
                }
            } catch (_: Exception) {
                file.delete()
            }
        }
        return list.sortedWith(compareBy({ it.getDigitSum() }, { it.parseTimestampMillis() }))
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
     * 嚴格落實經緯度與推送距離比對 + 7 位數編號總合值優先單一佇列排隊
     *
     * 1. 區域經緯度與外送/推送距離比對符合後，若發現區域內 TTS 封包重疊，
     * 2. 改為讀取封包上的編號/檔案夾上的編號 (UI 5 上 7 位數編號 etPacketCode)，
     * 3. 計算 1 到 7 位數總加值（總合值），總合值最小的為優先拉取與播放，總合值大的排在後面。
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

        val fetchRemote: ((List<TtsPacketRecord>) -> Unit) -> Unit = { callback ->
            if (context == null) {
                callback(emptyList())
            } else {
                BaichuanApiService.fetchTtsPacketsFromRemoteHub(callback)
            }
        }

        fetchRemote { remotePackets ->
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
            safeLogD("TtsRepository", "🔍 [TTS 經緯度與距離比對 + 7位數編號總合值優先佇列]")
            safeLogD("TtsRepository", "• 當前 GPS 座標: ($effectiveLat, $effectiveLng) | 設定搜尋半徑: $radiusKm km")
            safeLogD("TtsRepository", "• 倉庫拉取封包總筆數: ${allPackets.size} 筆 (遠端: ${remotePackets.size}, 本地: ${localPackets.size}, 歸檔區: ${localArchivedIds.size})")
            safeLogD("TtsRepository", "--------------------------------------------------------------------------------")

            val qualifiedPackets = mutableListOf<Pair<TtsPacketRecord, Double>>()

            for (packet in allPackets) {
                if (packet.isArchived || packet.remainingCount <= 0 || localArchivedIds.contains(packet.packetId)) {
                    safeLogD("TtsRepository", "  - 封包 [${packet.packetId}]: 已歸零/已歸檔 ➔ ❌ 略過")
                    continue
                }

                val pLat = packet.latitude
                val pLng = packet.longitude
                if (pLat == null || pLng == null || pLat.isNaN() || pLng.isNaN()) {
                    safeLogD("TtsRepository", "  - 封包 [${packet.packetId}]: 缺失或無效 GPS 座標 ➔ ❌ 拒絕比對")
                    continue
                }
                
                val dist = DistanceUtils.calculateDistance(effectiveLat, effectiveLng, pLat, pLng)
                val pktTriggerRadius = packet.radiusKm ?: radiusKm

                val isMatched = dist <= pktTriggerRadius
                safeLogD("TtsRepository", "  - 封包 [${packet.packetId}]: 7位數字總合值: ${packet.getDigitSum()} (編號: ${packet.packetCode.ifBlank { packet.packetId }}) | 距離 ${String.format(Locale.getDefault(), "%.2f", dist)} km | 觸發半徑: $pktTriggerRadius km | 剩餘次數: ${packet.remainingCount} | Audit Timestamp: [${packet.timestamp}] ➔ ${if (isMatched) "✅ 符合範圍，納入重疊比對佇列" else "❌ 超出範圍 (拒絕)"}")

                if (isMatched) {
                    qualifiedPackets.add(Pair(packet, dist))
                }
            }

            // 區域內重疊封包比對：依據 UI 5 上的 7 位數編號 1 到 7 位數總加值（總合值）進行排序：總合值小的排前面優先拉取，大的排後面
            val sortedQualified = qualifiedPackets.sortedWith(
                compareBy<Pair<TtsPacketRecord, Double>> { (packet, _) -> packet.getDigitSum() }
                    .thenBy { (packet, _) -> packet.parseTimestampMillis() }
            )
            val sortedPackets = sortedQualified.map { it.first }

            safeLogD("TtsRepository", "--------------------------------------------------------------------------------")
            if (sortedPackets.isEmpty()) {
                safeLogD("TtsRepository", "⚠️ 當前涵蓋範圍內無合格 TTS 廣播封包，維護靜默狀態。")
                safeLogD("TtsRepository", "================================================================================")
                onResult(null, emptyList())
                return@fetchRemote
            }

            // 每次只允許解鎖並播報【7位數總合值最小/最優先】的第一個封包
            val topPriorityPacket = sortedPackets.first()

            safeLogD("TtsRepository", "🎯 🔓 [單一佇列鎖定 - 7位數編號總合值最優先封包 (Priority #1, 總合值=${topPriorityPacket.getDigitSum()})]")
            safeLogD("TtsRepository", "• 播報目標: PacketID [${topPriorityPacket.packetId}] | 7位數編號: [${topPriorityPacket.packetCode.ifBlank { topPriorityPacket.packetId }}] | 總合值: ${topPriorityPacket.getDigitSum()} | Timestamp: [${topPriorityPacket.timestamp}] | 剩餘次數: ${topPriorityPacket.remainingCount}")
            safeLogD("TtsRepository", "• 純化播報文字 (純內容無雜質): \"${topPriorityPacket.getPureText()}\"")

            if (sortedPackets.size > 1) {
                safeLogD("TtsRepository", "🔒 [其餘重疊封包佇列鎖定中] 以下 ${sortedPackets.size - 1} 筆總合值較大/順位較後封包保持等待，直到 [${topPriorityPacket.packetId}] 倒數歸零後始解鎖:")
                sortedPackets.drop(1).forEachIndexed { index, p ->
                    safeLogD("TtsRepository", "  - 等待順位 #${index + 2}: PacketID [${p.packetId}] | 編號: [${p.packetCode.ifBlank { p.packetId }}] | 總合值: ${p.getDigitSum()} | Timestamp: [${p.timestamp}] | 剩餘: ${p.remainingCount} 次 | 純文字: \"${p.getPureText()}\"")
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
        // 印出當前呼叫堆疊與來源路徑，精準追查封包來源
        try {
            val sourceTrace = Thread.currentThread().stackTrace
            val callerInfo = sourceTrace.getOrNull(3)?.let { 
                "File: ${it.fileName}, Method: ${it.methodName}, Line: ${it.lineNumber}" 
            } ?: "Unknown Source"
            safeLogD("PacketSourceTracker", "=== 攔截到 TTS 封包扣減與覆寫 (TtsRepositoryManager.consumePacketPlaybackCount) ===")
            safeLogD("PacketSourceTracker", "來源路徑詳情 -> $callerInfo")
        } catch (_: Throwable) {
            // Fallback for non-Android unit test runtimes
        }

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

        // val targetRecord = record ?: TtsPacketRecord(packetId = packetId, text = "", remainingCount = 100) // 應禁止建立假資料
        val targetRecord = record ?: return

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
            // 次數 == 0：倒數歸零直接拋棄，不做儲存動作，避免幹擾
            if (file.exists()) file.delete()

            val archiveDir = File(dir, "archived")
            val archiveFile = File(archiveDir, "$packetId.json")
            if (archiveFile.exists()) archiveFile.delete()

            safeLogD("TtsRepository", "🏁 🔓 [倒數歸零直接拋棄] PacketID: [$packetId] 剩餘次數歸零 (remainingCount=0)，已從活躍倉庫與本地實體檔案徹底刪除拋棄，不做儲存動作！推回後端倉庫取消清理 ➔ 即刻解鎖時間戳第二順位之封包！ | Audit Timestamp: [$nowTimestamp]")

            BaichuanApiService.deleteActiveTtsPacketAndPushToArchive(packetId, jsonOutput, {
                safeLogD("TtsRepository", "✅ [後端取消與活躍區清理成功] PacketID: [$packetId] 活躍檔已從後端移出，解鎖佇列下一順位。")
                onComplete?.invoke(updatedRecord, true)
            }, { err ->
                safeLogE("TtsRepository", "⚠️ [後端取消回應] PacketID: [$packetId]: $err")
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
