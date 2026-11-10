package com.example.myapplication01

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

/**
 * TtsManager：TTS 語音引擎管理類別
 * 實作 TextToSpeech.OnInitListener，支援倒數第二字攔截與背景發送經緯度比對機制
 */
class TtsManager(
    context: Context,
    private val onInitComplete: (() -> Unit)? = null
) : TextToSpeech.OnInitListener {

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
    private var isSequencingActive = false
    private var isBlessingDone = false
    private var isBackendFetchPending = false
    private var sequenceCompleteCallback: (() -> Unit)? = null

    /**
     * 卡片重新整理回調：當背景拉回與列表更新完成時觸發
     */
    var onCardRefreshTriggered: (() -> Unit)? = null

    /**
     * 推入 FIFO 語音播報佇列 (標準無中斷 FIFO 佇列)
     */
    fun enqueueFifoPayload(text: String, utteranceId: String = "TTS_FIFO_PAYLOAD") {
        if (text.isBlank()) return
        fifoPayloadQueue.add(Pair(text, utteranceId))
        Log.d("TtsManager", "📥 [FIFO 佇列入隊] \"$text\" (當前佇列長度: ${fifoPayloadQueue.size})")
        mainHandler.post { processNextFifoPayload() }
    }

    private fun processNextFifoPayload() {
        if (isSpeaking || isFifoProcessing) return
        val item = fifoPayloadQueue.poll() ?: return
        isFifoProcessing = true
        val (text, utteranceId) = item
        Log.d("TtsManager", "▶️ [FIFO 順序播放開始] utteranceId=$utteranceId, 內容=\"$text\"")
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

        // 立即在主執行緒讀取本地快取資料並執行介面更新
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
            val langCode = GlobalState.selectedLanguageCode
            val locale = getLocaleFromLangCode(langCode)
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TtsManager", "語言 $locale 不支援，降級至 Locale.TAIWAN")
                tts?.setLanguage(Locale.TAIWAN)
            }

            setupUtteranceProgressListener()

            isReady = true
            Log.d("TtsManager", "TTS 引擎初始化成功！")

            mainHandler.post {
                onInitComplete?.invoke()
            }

            pendingSpeech?.let { (text, queueMode, utteranceId) ->
                pendingSpeech = null
                speak(text, queueMode, utteranceId)
            }
        } else {
            isReady = false
            Log.e("TtsManager", "TTS 初始化失敗, status=$status")
        }
    }

    /**
     * 設定 UtteranceProgressListener 以捕捉進度事件
     */
    private fun setupUtteranceProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                isSpeaking = true
                Log.d("TtsManager", "🎙️ TTS 開始播報 utteranceId=$utteranceId, 全文長度=${currentSpeakingText.length}")
            }

            override fun onDone(utteranceId: String) {
                isSpeaking = false
                isFifoProcessing = false
                Log.d("TtsManager", "✅ TTS 播報完成 utteranceId=$utteranceId，解鎖下一次播報狀態")

                // 語音自然播放完成時觸發倒數次數扣減
                if (utteranceId.startsWith("TTS_") || utteranceId == "DEITY_BLESSING" || utteranceId == "AD_HURLUNGCHUANG_01") {
                    HurlungchuangTestManager.onPlaybackCompleted()
                    TtsAdManager.onAdPlayed("AD_HURLUNGCHUANG_01")
                }

                if (isSequencingActive) {
                    if (utteranceId == "DEITY_BLESSING") {
                        isBlessingDone = true
                        Log.d("TtsManager", "✨ [心靈祝福辭撥完] 心靈祝福辭播報完成！啟動續撥功能...")
                        mainHandler.post { playNextSequenceItem() }
                    } else if (utteranceId.startsWith("BROADCAST_SEQ_")) {
                        Log.d("TtsManager", "📦 [封包撥報序] 當前封包播放完畢，續撥下一筆...")
                        mainHandler.post { playNextSequenceItem() }
                    }
                }

                // 順序播放 FIFO 佇列下一筆
                mainHandler.post { processNextFifoPayload() }
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
                        mainHandler.post { playNextSequenceItem() }
                    } else if (utteranceId.startsWith("BROADCAST_SEQ_")) {
                        mainHandler.post { playNextSequenceItem() }
                    }
                }

                mainHandler.post { processNextFifoPayload() }
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
     * 播報指定語音文字
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH, utteranceId: String = "TTS_ID") {
        currentSpeakingText = text

        if (isReady && tts != null) {
            try {
                val params = HashMap<String, String>()
                params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = utteranceId
                @Suppress("DEPRECATION")
                tts?.speak(text, queueMode, params)
            } catch (e: Exception) {
                Log.e("TtsManager", "播報語音時發生異常: ${e.message}", e)
            }
        } else {
            Log.w("TtsManager", "TTS 尚未準備就緒，已快取播報請求: $text")
            pendingSpeech = Triple(text, queueMode, utteranceId)
        }
    }

    /**
     * 播報卡片廣播內容 (含分類標題、距離、詳細內容與自訂間隔秒數/毫秒數)
     *
     * @param categoryTitle 分類標題 (例如 "生鮮外送剛需一區")
     * @param distanceKm 距離公里數 (例如 1.8)
     * @param content 詳細內容 (例如 "目前有急件報表等待確認")
     * @param silenceMs 播報結束後的停頓靜音毫秒數 (預設 1000ms)
     * @param utteranceId 播報識別碼
     */
    fun playCardBroadcast(
        categoryTitle: String,
        distanceKm: Double,
        content: String,
        silenceMs: Long = 1000L,
        utteranceId: String = "CARD_BROADCAST"
    ) {
        val distanceText = if (distanceKm > 0) {
            val formatted = String.format(Locale.getDefault(), "%.1f", distanceKm)
            "距離 ${formatted} 公里"
        } else ""

        val parts = mutableListOf<String>()
        if (categoryTitle.isNotBlank()) parts.add(categoryTitle)
        if (distanceText.isNotBlank()) parts.add(distanceText)
        if (content.isNotBlank()) parts.add(content)

        val fullBroadcastText = parts.joinToString("，")

        speak(fullBroadcastText, TextToSpeech.QUEUE_FLUSH, utteranceId)

        if (silenceMs > 0 && isReady && tts != null) {
            try {
                tts?.playSilentUtterance(silenceMs, TextToSpeech.QUEUE_ADD, "${utteranceId}_SILENCE")
            } catch (e: Exception) {
                Log.e("TtsManager", "播報靜音停頓失敗: ${e.message}", e)
            }
        }
    }

    /**
     * 重載：支援帶入 RequirementCard / CardModel 實體物件進行卡片語音播報
     */
    fun playCardBroadcast(
        card: RequirementCard,
        distanceKm: Double? = null,
        silenceMs: Long = 1000L,
        utteranceId: String = "CARD_BROADCAST"
    ) {
        val calculatedDistance = distanceKm ?: run {
            val userLat = GlobalState.globalLatitude ?: 0.0
            val userLng = GlobalState.globalLongitude ?: 0.0
            if (userLat != 0.0 && userLng != 0.0 && card.gps_lat != 0.0 && card.gps_lng != 0.0) {
                DistanceUtils.calculateDistance(card.gps_lat, card.gps_lng, userLat, userLng)
            } else {
                0.0
            }
        }
        val title = card.category_tag.ifBlank { "卡片廣播" }
        playCardBroadcast(title, calculatedDistance, card.description, silenceMs, utteranceId)
    }

    /**
     * 分段播報卡片廣播內容並在關鍵段落（第二段前）觸發回調 Lambda (例如觸發倒數扣減或雲端 API 拉取)
     *
     * @param categoryTitle 分類標題
     * @param distanceKm 距離公里數
     * @param content 詳細內容
     * @param silenceMs 段落之間的靜音停頓毫秒數 (預設 1000ms)
     * @param onNearEndTriggered 播報至關鍵段落時的回調函數
     */
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

        val formattedDistance = if (distanceKm > 0) {
            String.format(Locale.getDefault(), "%.1f", distanceKm)
        } else "0.0"

        val part1Text = "附近剛需外送通知：$categoryTitle"
        val part2Text = "距離您約 $formattedDistance 公里，內容為：$content"

        currentSpeakingText = "$part1Text $part2Text"

        try {
            // 1. 播報第一段
            val params1 = HashMap<String, String>()
            params1[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "utterance_title"
            @Suppress("DEPRECATION")
            tts?.speak(part1Text, TextToSpeech.QUEUE_FLUSH, params1)

            // 2. 插入靜音間隔
            if (silenceMs > 0) {
                tts?.playSilentUtterance(silenceMs, TextToSpeech.QUEUE_ADD, "utterance_silence")
            }

            // 3. 觸發關鍵段落回調與印出 LOG
            mainHandler.post {
                Log.d("TTS_TRIGGER", "🔥 倒數關鍵點觸發成功！準備拉取/扣減遠端資料")
                onNearEndTriggered.invoke()
            }

            // 4. 播報第二段
            val params2 = HashMap<String, String>()
            params2[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "utterance_content"
            @Suppress("DEPRECATION")
            tts?.speak(part2Text, TextToSpeech.QUEUE_ADD, params2)

        } catch (e: Exception) {
            Log.e("TtsManager", "播報帶回調卡片時發生異常: ${e.message}", e)
        }
    }

    /**
     * 播放祝福辭，並自動連動後端連結請求發回封包撥報序，
     * 等待心靈祝福辭撥完後，自動續撥並序接播放後端回傳的封包直到播完。
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
        broadcastQueue.clear()
        isSequencingActive = true
        isBlessingDone = false
        isBackendFetchPending = true
        sequenceCompleteCallback = onSequenceComplete

        Log.d("TtsManager", "🔗 [發送經緯度比對與後端連結] 座標($latitude, $longitude)，發送請求比對，等待發回封包與排序，並優先播報心靈祝福辭: \"$blessingText\"")

        // 1. 經緯度比對：檢查區域 6KM 封包比對
        val localPayload = HurlungchuangTestManager.checkAndPullPayload(latitude, longitude)

        // 2. 連結後端請求發回封包撥報序與排序
        BaichuanApiService.fetchNearbyCards(latitude, longitude, radiusKm, categories) { cloudCards ->
            mainHandler.post {
                isBackendFetchPending = false
                val texts = mutableListOf<String>()

                if (!localPayload.isNullOrBlank()) {
                    texts.add(localPayload)
                }

                val cloudTexts = cloudCards.map { card ->
                    val dist = if (card.gps_lat != 0.0 && card.gps_lng != 0.0) {
                        DistanceUtils.calculateDistance(card.gps_lat, card.gps_lng, latitude, longitude)
                    } else 0.0
                    val distStr = if (dist > 0) String.format(Locale.getDefault(), "距離 %.1f 公里", dist) else ""
                    listOf(card.category_tag, distStr, card.description).filter { it.isNotBlank() }.joinToString("，")
                }
                texts.addAll(cloudTexts)

                broadcastQueue.addAll(texts)
                onBackendResponse?.invoke(cloudCards)
                Log.d("TtsManager", "🔗 [後端連結請求發回] 經緯度比對符合，成功發回封包與排序，共收到 ${texts.size} 筆，已加入續撥佇列等待心靈祝福辭撥完後續接播放")

                // 若心靈祝福辭在後端請求返回前已撥完，在此立即啟動續撥
                if (isBlessingDone && isSequencingActive) {
                    playNextSequenceItem()
                }
            }
        }

        // 3. 優先播報心靈祝福辭
        speak(blessingText, TextToSpeech.QUEUE_FLUSH, "DEITY_BLESSING")
    }

    /**
     * 手動將後端連結請求發回之封包撥報序加入佇列 (若序接撥報已啟動，將在當前發音結束後自動續撥)
     */
    fun enqueueBackendPacketTexts(
        texts: List<String>,
        onSequenceComplete: (() -> Unit)? = null
    ) {
        if (texts.isEmpty()) return
        if (onSequenceComplete != null) {
            val prev = sequenceCompleteCallback
            sequenceCompleteCallback = {
                prev?.invoke()
                onSequenceComplete.invoke()
            }
        }
        broadcastQueue.addAll(texts)
        isSequencingActive = true
        Log.d("TtsManager", "📥 [手動入隊] 追加 ${texts.size} 筆後端發回封包至撥報序佇列 (當前佇列共: ${broadcastQueue.size} 筆)")
        if (isBlessingDone && !isSpeaking) {
            playNextSequenceItem()
        }
    }

    /**
     * 序接播放下一筆後端連線請求發回封包撥報序
     */
    private fun playNextSequenceItem() {
        if (!isSequencingActive) return

        if (broadcastQueue.isNotEmpty()) {
            val nextText = broadcastQueue.removeAt(0)
            val seqId = "BROADCAST_SEQ_${System.currentTimeMillis()}"
            Log.d("TtsManager", "▶️ [續撥功能] 心靈祝福辭/前段已播完，開始序接播放下一筆封包撥報序: \"$nextText\"")
            speak(nextText, TextToSpeech.QUEUE_FLUSH, seqId)
        } else if (!isBackendFetchPending) {
            Log.d("TtsManager", "🏁 [序接播放完成] 所有後端連線請求發回封包撥報序已全數續撥完畢！")
            isSequencingActive = false
            val callback = sequenceCompleteCallback
            sequenceCompleteCallback = null
            callback?.invoke()
        } else {
            Log.d("TtsManager", "⌛ [等待後端發回] 心靈祝福辭已撥完，正在等待後端連線請求發回封包歸隊...")
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
