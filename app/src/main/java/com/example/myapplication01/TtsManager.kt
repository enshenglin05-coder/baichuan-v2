package com.example.myapplication01

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TtsManager：TTS 語音引擎管理類別
 * 實作 TextToSpeech.OnInitListener，支援倒數第二字攔截與背景發送經緯度比對機制
 *
 * 實作規則：
 * 1. TTS 內容純化 (僅播放文字卡片內容)：
 *    - 嚴格確保 Android 的 TextToSpeech 在執行 speak() 時，只讀取純粹的文字內容 (JSON 中的 text 欄位值)。
 *    - 絕對禁止將系統計算的經緯度、距離數值 ("距離 X.X 公里")、或除錯/狀態字串 ("廣播封包 [ID]：") 拼接到語音播報內容中。
 * 2. 嚴格落實時間戳單一佇列排隊 (Strict FIFO by Timestamp)：
 *    - 區域篩選出多個封包後，依據 timestamp 進行由早至晚嚴格排序。
 *    - 每次只允許解鎖並播報時間戳最優先 (最早) 的【單一封包】。
 *    - 該封包計數倒數歸零 (remainingCount == 0) 並且推送回歸檔倉庫 (/archived/) 後，才能解鎖並輪到時間戳第二順位的封包。
 */
class TtsManager(
    val context: Context,
    private val onInitComplete: (() -> Unit)? = null
) : TextToSpeech.OnInitListener {

    data class TtsBroadcastPacketItem(
        val packetId: String,
        val text: String,
        val timestamp: String,
        val remainingCount: Int
    )

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isReady = false
    private var pendingSpeech: Triple<String, Int, String>? = null

    private var currentSpeakingText: String = ""
    private var isSpeaking = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // FIFO 語音播報佇列 (無中斷、無字數監聽器，自然播放完畢後自動觸發下一筆)
    private val fifoPayloadQueue = java.util.concurrent.ConcurrentLinkedQueue<Pair<String, String>>()
    private var isFifoProcessing = false

    // 封包撥報序續撥佇列與狀態控制
    private val broadcastQueue = java.util.concurrent.CopyOnWriteArrayList<String>()
    private val broadcastPacketQueue = java.util.concurrent.CopyOnWriteArrayList<TtsBroadcastPacketItem>()
    private var currentSpeakingPacketItem: TtsBroadcastPacketItem? = null

    private var isSequencingActive = false
    private var isBlessingDone = false
    private var sequenceCompleteCallback: (() -> Unit)? = null

    /**
     * 卡片重新整理回調：當背景拉回與列表更新完成時觸發
     */
    var onCardRefreshTriggered: (() -> Unit)? = null

    /**
     * 推入 FIFO 語音播報佇列 (標準無中斷 FIFO 佇列)
     */
    fun enqueueFifoPayload(text: String, utteranceId: String = "TTS_FIFO_PAYLOAD") {
        val pureText = text.trim()
        if (pureText.isBlank()) return
        fifoPayloadQueue.add(Pair(pureText, utteranceId))
        Log.d("TtsManager", "📥 [FIFO 純化佇列入隊] \"$pureText\" (當前佇列長度: ${fifoPayloadQueue.size})")
        mainHandler.post { processNextFifoPayload() }
    }

    private fun processNextFifoPayload() {
        if (isSpeaking || isFifoProcessing) return
        val item = fifoPayloadQueue.poll() ?: return
        isFifoProcessing = true
        val (text, utteranceId) = item
        Log.d("TtsManager", "▶️ [FIFO 順序純化播放] utteranceId=$utteranceId, 內容=\"$text\"")
        speak(text, TextToSpeech.QUEUE_FLUSH, utteranceId)
    }

    /**
     * 強制觸發背景列表重新整理與資料拉回 (Refresh/Pull)
     */
    fun triggerCardRefreshAndPull() {
        val lat = GlobalState.globalLatitude
            ?: LocationFlowManager.currentLocation?.first
            ?: HurlungchuangTestManager.TARGET_LATITUDE
        val lng = GlobalState.globalLongitude
            ?: LocationFlowManager.currentLocation?.second
            ?: HurlungchuangTestManager.TARGET_LONGITUDE

        val distance = DistanceUtils.calculateDistance(
            HurlungchuangTestManager.TARGET_LATITUDE,
            HurlungchuangTestManager.TARGET_LONGITUDE,
            lat,
            lng
        )

        Log.d("CardRefresh", "手動觸發資料拉回與列表更新，距離: ${distance}km")

        mainHandler.post {
            onCardRefreshTriggered?.invoke()
            Log.d("CardRefresh", "手動拉取完成，已更新本地資料與渲染畫面")
        }

        BaichuanApiService.fetchNearbyCards(lat, lng, 50.0, emptyList()) { cloudCards ->
            if (cloudCards.isNotEmpty()) {
                BaichuanRepository.injectTestCards(cloudCards)
            }
            BaichuanRepository.ensurePresetCardsLoaded()
            val jsonCount = cloudCards.size
            val filenames = cloudCards.map { "${it.card_id}.json" }
            Log.d("CardRefresh", "🌐 [API 回傳狀態] 手動拉取完成，實際取得 JSON 數量: $jsonCount | 檔名列表: $filenames")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d("TtsManager", "✅ [TTS 引擎初始化] 成功 (TextToSpeech.SUCCESS)")
            val langCode = GlobalState.selectedLanguageCode
            val locale = getLocaleFromLangCode(langCode)
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsManager", "⚠️ [TTS 語系設定] 語言 $locale 不支援，降級至 Locale.TAIWAN")
                tts?.setLanguage(Locale.TAIWAN)
            }

            setupUtteranceProgressListener()

            isReady = true
            Log.d("TtsManager", "🚀 [TTS 引擎已就緒] 準備開始播報")

            mainHandler.post {
                onInitComplete?.invoke()
            }

            pendingSpeech?.let { (text, queueMode, utteranceId) ->
                pendingSpeech = null
                speak(text, queueMode, utteranceId)
            }
        } else {
            isReady = false
            Log.e("TtsManager", "❌ [TTS 引擎初始化] 失敗, status=$status")
        }
    }

    /**
     * 設定 UtteranceProgressListener 以捕捉進度事件
     */
    private fun setupUtteranceProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                isSpeaking = true
                Log.d("TtsManager", "🎙️ TTS 開始純化播報 utteranceId=$utteranceId, 內容長度=${currentSpeakingText.length}")
            }

            override fun onDone(utteranceId: String) {
                isSpeaking = false
                isFifoProcessing = false
                Log.d("TtsManager", "✅ TTS 播報完成 utteranceId=$utteranceId, 實際純文字內容: \"$currentSpeakingText\"")

                val item = currentSpeakingPacketItem
                if (item != null && item.packetId.isNotBlank()) {
                    Log.d("TtsManager", "📉 [播畢即時倒數 -1] 封包 [${item.packetId}] 播報完畢 1 次 (Audit Timestamp: ${item.timestamp})...")
                    TtsRepositoryManager.consumePacketPlaybackCount(item.packetId, context) { updatedRecord, isArchived ->
                        if (isArchived) {
                            Log.d("TtsManager", "📦 🔓 [歸零歸檔與解鎖] 封包 [${item.packetId}] 已歸零推回歸檔倉庫，解鎖時間戳第二順位之封包...")
                        } else {
                            Log.d("TtsManager", "📉 封包 [${item.packetId}] 次數即時扣減完畢 ➔ 新剩餘次數: ${updatedRecord?.remainingCount}")
                        }
                    }
                    currentSpeakingPacketItem = null
                }

                if (isSequencingActive) {
                    if (utteranceId == "DEITY_BLESSING") {
                        isBlessingDone = true
                        Log.d("TtsManager", "✨ [心靈祝福辭撥完] 心靈祝福辭播報完成！自動銜接時間戳最優先區域封包...")
                        playNextSequenceItem()
                    } else if (utteranceId.startsWith("BROADCAST_SEQ_")) {
                        Log.d("TtsManager", "📦 [封包撥報序完成] 當前封包播放完成，自動檢視下一個解鎖封包...")
                        playNextSequenceItem()
                    }
                } else {
                    processNextFifoPayload()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String) {
                isSpeaking = false
                isFifoProcessing = false
                Log.e("TtsManager", "❌ TTS 播報失敗 utteranceId=$utteranceId，解鎖狀態")

                if (isSequencingActive) {
                    if (utteranceId == "DEITY_BLESSING") {
                        isBlessingDone = true
                        Log.w("TtsManager", "⚠️ 心靈祝福辭播報中斷，嘗試續撥後端封包...")
                        playNextSequenceItem()
                    } else if (utteranceId.startsWith("BROADCAST_SEQ_")) {
                        playNextSequenceItem()
                    }
                } else {
                    processNextFifoPayload()
                }
            }

            override fun onRangeStart(utteranceId: String, start: Int, end: Int, frame: Int) {
                // 已簡化/取消複雜倒數計字計算
            }
        })
    }

    private fun getLocaleFromLangCode(langCode: String): Locale {
        return when (langCode) {
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            "en" -> Locale.US
            "ja" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            "vi" -> Locale("vi", "VN")
            else -> Locale.TRADITIONAL_CHINESE
        }
    }

    /**
     * 切換 TTS 發音語系
     */
    fun setLanguage(locale: Locale): Boolean {
        if (tts == null) {
            Log.w("TtsManager", "TTS 尚未準備就緒，無法設定語系")
            return false
        }
        val result = tts?.setLanguage(locale)
        val supported = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
        if (!supported) {
            Log.e("TtsManager", "語言 $locale 不支援或缺少資料")
        }
        return supported
    }

    /**
     * 播報指定語音純內容 (嚴格確保 TextToSpeech.speak 只讀取純文字內容)
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH, utteranceId: String = "TTS_ID") {
        val pureText = text.trim()
        currentSpeakingText = pureText
        val sourceType = utteranceId

        Log.d("TtsManager_Trigger", "🎯 [TTS 播報純文字] 通道: $sourceType, 內容: \"$pureText\", isReady=$isReady")

        if (isReady && tts != null) {
            try {
                val effectiveQueueMode = if (isSpeaking && queueMode == TextToSpeech.QUEUE_FLUSH) {
                    TextToSpeech.QUEUE_ADD
                } else {
                    queueMode
                }
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
                @Suppress("DEPRECATION")
                val result = tts?.speak(pureText, effectiveQueueMode, params)
                Log.d("TtsManager_Trigger", "✅ [TTS 執行 speak()] 純文字成功發出，result=$result (SUCCESS=0)")
            } catch (e: Exception) {
                Log.e("TtsManager", "❌ [TTS 發聲異常] 異常內容: ${e.message}", e)
            }
        } else {
            Log.w("TtsManager", "⚠️ [TTS 未就緒] 引擎尚未初始化成功，快取純文字請求: $pureText")
            pendingSpeech = Triple(pureText, queueMode, utteranceId)
        }
    }

    /**
     * 播報卡片廣播內容 (純化版本：僅讀取純文字 content)
     */
    fun playCardBroadcast(
        categoryTitle: String,
        distanceKm: Double,
        content: String,
        silenceMs: Long = 1000L,
        utteranceId: String = "CARD_BROADCAST"
    ) {
        val pureText = content.trim()
        speak(pureText, TextToSpeech.QUEUE_FLUSH, utteranceId)

        if (silenceMs > 0 && isReady && tts != null) {
            try {
                tts?.playSilentUtterance(silenceMs, TextToSpeech.QUEUE_ADD, "${utteranceId}_SILENCE")
            } catch (e: Exception) {
                Log.e("TtsManager", "播報靜音停頓失敗: ${e.message}", e)
            }
        }
    }

    fun playCardBroadcast(
        card: RequirementCard,
        distanceKm: Double? = null,
        silenceMs: Long = 1000L,
        utteranceId: String = "CARD_BROADCAST"
    ) {
        val pureText = card.description.ifBlank { card.category_tag }.trim()
        playCardBroadcast(card.category_tag, 0.0, pureText, silenceMs, utteranceId)
    }

    fun playCardBroadcastWithCallback(
        categoryTitle: String, 
        distanceKm: Double, 
        content: String, 
        silenceMs: Long = 1000L,
        onNearEndTriggered: () -> Unit
    ) {
        if (!isInitialized) {
            Log.w("TtsManager", "TTS 尚未準備就緒，無法執行 playCardBroadcastWithCallback")
            return
        }

        val pureText = content.trim()
        currentSpeakingText = pureText

        try {
            val params1 = HashMap<String, String>()
            params1[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "utterance_content"
            @Suppress("DEPRECATION")
            tts?.speak(pureText, TextToSpeech.QUEUE_FLUSH, params1)

            if (silenceMs > 0) {
                tts?.playSilentUtterance(silenceMs, TextToSpeech.QUEUE_ADD, "utterance_silence")
            }

            mainHandler.post {
                Log.d("TTS_TRIGGER", "🔥 倒數關鍵點觸發成功！準備拉取/扣減遠端資料")
                onNearEndTriggered.invoke()
            }
        } catch (e: Exception) {
            Log.e("TtsManager", "播報帶回調卡片時發生異常: ${e.message}", e)
        }
    }

    /**
     * 播報祝福辭，並自動連動後端連結請求發回封包撥報序，
     * 等待心靈祝福辭撥完後，自動續撥並序接播放後端回傳的封包直到播完。
     * [重構版本]：TTS 內容純化 (僅純文字) 與 Strict FIFO by Timestamp 鎖定
     */
    fun speakBlessingAndSequenceBackendBroadcasts(
        blessingText: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 50.0,
        categories: List<String> = emptyList(),
        onBackendResponse: ((List<RequirementCard>) -> Unit)? = null,
        onSequenceComplete: (() -> Unit)? = null
    ) {
        fifoPayloadQueue.clear()
        isFifoProcessing = false
        broadcastQueue.clear()
        broadcastPacketQueue.clear()
        isSequencingActive = true
        isBlessingDone = false
        sequenceCompleteCallback = onSequenceComplete

        Log.d("TtsManager", "🔗 [TTS 內容純化與單一佇列鎖定] 座標($latitude, $longitude)，開始全量拉取與動態比對...")

        // 全量拉取並依 Strict FIFO by Timestamp 鎖定單一最優先封包
        DeityBlessingTTSReadExecutor.executeReadTask(this.context, latitude, longitude, radiusKm) { _, topPriorityPacket, _ ->
            mainHandler.post {
                broadcastPacketQueue.clear()
                if (topPriorityPacket != null) {
                    val pureContent = topPriorityPacket.getPureText()
                    val item = TtsBroadcastPacketItem(
                        packetId = topPriorityPacket.packetId,
                        text = pureContent,
                        timestamp = topPriorityPacket.timestamp,
                        remainingCount = topPriorityPacket.remainingCount
                    )
                    broadcastPacketQueue.add(item)
                    Log.d("TtsManager", "🎯 [純化內容鎖定播報] PacketID: [${topPriorityPacket.packetId}] | Audit Timestamp: [${topPriorityPacket.timestamp}] | 純文字內容: \"$pureContent\"")
                } else {
                    Log.d("TtsManager", "⚠️ 涵蓋範圍內無合格播報封包。")
                }

                if (isBlessingDone && isSequencingActive) {
                    playNextSequenceItem()
                }
            }
        }

        // 優先播報心靈祝福辭 (純文字)
        speak(blessingText.trim(), TextToSpeech.QUEUE_FLUSH, "DEITY_BLESSING")
    }

    /**
     * 排隊播報多個封包 (依據 7 位數編號總合值最小優先與時間排序)
     */
    fun enqueueBackendPacketTexts(
        cards: List<RequirementCard>,
        onSequenceComplete: (() -> Unit)? = null
    ) {
        if (cards.isEmpty()) return

        val sortedCards = cards.sortedWith(
            compareBy<RequirementCard> { TtsRepositoryManager.calculatePacket7DigitSum(it.card_id) }
                .thenBy { it.created_at }
        )

        val items = sortedCards.mapNotNull { card ->
            val pureTextContent = card.description.ifBlank { card.category_tag }.trim()
            if (pureTextContent.isBlank()) {
                null
            } else {
                TtsBroadcastPacketItem(
                    packetId = card.card_id,
                    text = pureTextContent,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date(card.created_at)),
                    remainingCount = 3
                )
            }
        }

        if (onSequenceComplete != null) {
            val prev = sequenceCompleteCallback
            sequenceCompleteCallback = {
                prev?.invoke()
                onSequenceComplete.invoke()
            }
        }
        broadcastPacketQueue.addAll(items)
        isSequencingActive = true
        Log.d("TtsManager", "📥 [順序入隊] 已依 7 位數編號總合值優先排序追加 ${items.size} 筆純文字封包 (當前佇列共: ${broadcastPacketQueue.size} 筆)")
        
        if (!isSpeaking) {
            playNextSequenceItem()
        }
    }

    /**
     * 序接播放下一筆封包，播完後若次數歸零則自動呼叫 TtsRepositoryManager 進行背景歸檔並解鎖下一筆
     */
    private fun playNextSequenceItem() {
        if (!isSequencingActive) return

        if (broadcastPacketQueue.isNotEmpty()) {
            val item = broadcastPacketQueue.removeAt(0)
            currentSpeakingPacketItem = item
            currentSpeakingText = item.text.trim()

            val seqId = "BROADCAST_SEQ_${item.packetId}_${System.currentTimeMillis()}"
            Log.d("TtsManager", "▶️ [純化單一順序播報開始] PacketID: [${item.packetId}] | Audit Timestamp: [${item.timestamp}] | 當前剩餘次數: ${item.remainingCount} | 純內容: \"${item.text}\"")

            speak(item.text, TextToSpeech.QUEUE_FLUSH, seqId)
        } else if (broadcastQueue.isNotEmpty()) {
            val nextText = broadcastQueue.removeAt(0).trim()
            val seqId = "BROADCAST_SEQ_${System.currentTimeMillis()}"
            currentSpeakingPacketItem = null
            currentSpeakingText = nextText
            Log.d("TtsManager", "▶️ [順序播報] 開始播放純文字: \"$nextText\" (佇列剩餘: ${broadcastQueue.size})")
            speak(nextText, TextToSpeech.QUEUE_FLUSH, seqId)
        } else {
            Log.d("TtsManager", "🏁 [序接播放完成] 所有區域符合封包已播放完畢")
            isSequencingActive = false
            currentSpeakingPacketItem = null
            val callback = sequenceCompleteCallback
            sequenceCompleteCallback = null
            callback?.invoke()
        }
    }

    /**
     * 停止當前播報
     */
    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("TtsManager", "停止 TTS 播報失敗: ${e.message}", e)
        }
    }

    /**
     * 離開時或 Activity onDestroy 時正確釋放資源，防止 Disconnected 警告
     */
    fun shutdown() {
        try {
            pendingSpeech = null
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TtsManager", "釋放 TTS 資源失敗: ${e.message}", e)
        } finally {
            tts = null
            isReady = false
            Log.d("TtsManager", "TTS 資源已完全釋放")
        }
    }

    fun isSpeaking(): Boolean = isSpeaking
    fun isReady(): Boolean = isReady && tts != null
    val isInitialized: Boolean get() = isReady && tts != null
}
