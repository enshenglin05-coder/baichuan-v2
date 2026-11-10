package com.example.myapplication01

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * DeityBlessingTTS：專屬神明祝福語 TTS 服務模組
 * 負責連動 UI 選定主題與多國語系發音，並掛載倒數第二字觸發與雲端空間比對邏輯
 */
object DeityBlessingTTS {

    private var ttsManager: TtsManager? = null
    var onRefreshRequested: (() -> Unit)? = null

    fun init(context: Context) {
        TtsAdManager.init(context)
        if (ttsManager == null || ttsManager?.isInitialized == false) {
            ttsManager = TtsManager(context.applicationContext).apply {
                onCardRefreshTriggered = {
                    onRefreshRequested?.invoke()
                }
            }
        }
    }

    fun updateLanguage() {
        val langCode = GlobalState.selectedLanguageCode
        val locale = when (langCode) {
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            "en" -> Locale.US
            "ja" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            "vi" -> Locale("vi", "VN")
            else -> Locale.TRADITIONAL_CHINESE
        }
        ttsManager?.setLanguage(locale)
    }

    /**
     * 前台手動觸發（當使用者主動點擊「發布」或「刷新」時呼叫）：
     * 傳送本地經緯度座標，進行 6KM Haversine 距離檢查，符合即拉取封包並推入 FIFO 佇列順序播報
     */
    fun triggerUserActionPushPull(context: Context, userLat: Double?, userLng: Double?) {
        init(context)
        updateLanguage()
        val lat = userLat ?: GlobalState.globalLatitude ?: LocationFlowManager.currentLocation?.first ?: 0.0
        val lng = userLng ?: GlobalState.globalLongitude ?: LocationFlowManager.currentLocation?.second ?: 0.0

        val payload = HurlungchuangTestManager.checkAndPullPayload(lat, lng)
        if (!payload.isNullOrBlank()) {
            ttsManager?.enqueueFifoPayload(payload, utteranceId = "TTS_GEOSPATIAL_PAYLOAD")
        }
    }

    /**
     * 播放當前選定神明的祝福詞 (相容舊介面)
     */
    fun speakCurrentBlessing(context: Context) {
        speakCustomOrCurrent(context, null)
    }

    /**
     * 播放自訂輸入文字或當前祝福詞
     */
    fun speakCustomOrCurrent(context: Context, customText: String? = null) {
        init(context)
        updateLanguage()
        val appContext = context.applicationContext

        // 綁定輸入字串：優先使用使用者自訂輸入文字，其次為當前神明祝福詞
        val textToSpeak = if (!customText.isNullOrBlank()) {
            customText
        } else {
            val cards = ReligionRepository.getCardsByLanguage(GlobalState.selectedLanguageCode)
            val selectedId = GlobalState.selectedDeityId
            val card = cards.find { it.id == selectedId }
            if (card != null) {
                appContext.getString(card.descResId)
            } else {
                GlobalState.selectedBlessingText ?: appContext.getString(R.string.msg_publish_success_default)
            }
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val remainingCount = HurlungchuangTestManager.getRemainingCount()
        val secondToLastChar = if (textToSpeak.length >= 2) textToSpeak[textToSpeak.length - 2] else ' '

        Log.d(
            "TTS_DEBUG",
            "[$timestamp] [TTS_DEBUG] 觸發 TTS 播報 - 內容: '$textToSpeak', 剩餘次數: $remainingCount, 倒數第二個字: '$secondToLastChar'"
        )

        ttsManager?.speak(textToSpeak, utteranceId = "DEITY_BLESSING")
    }

    /**
     * 播放任意文字 (非阻塞)
     */
    fun speak(text: String, context: Context? = null) {
        context?.let { init(it) }
        updateLanguage()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val remainingCount = HurlungchuangTestManager.getRemainingCount()
        val secondToLastChar = if (text.length >= 2) text[text.length - 2] else ' '
        Log.d("TTS_DEBUG", "[$timestamp] [TTS_DEBUG] 播報任意文字: '$text', 剩餘次數: $remainingCount, 倒數第二個字: '$secondToLastChar'")
        ttsManager?.speak(text, utteranceId = "GENERIC_FEEDBACK")
    }

    fun speak(context: Context, text: String) {
        speak(text, context)
    }

    /**
     * 結合本地 TTS 與後端連結請求發回封包：
     * 1. 優先播報「心靈祝福詞」(utteranceId = "DEITY_BLESSING")。
     * 2. 非同步發送後端連線請求取得卡片/封包播報序，加入續撥佇列。
     * 3. 等待心靈祝福詞播完後順暢續撥後端封包內容。
     * 4. 序接播放完成後觸發 onSequenceComplete 回調。
     */
    fun speakBlessingAndSequenceBackendBroadcasts(
        context: Context,
        customBlessingText: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radiusKm: Double = 50.0,
        categories: List<String> = emptyList(),
        onBackendResponse: ((List<RequirementCard>) -> Unit)? = null,
        onSequenceComplete: (() -> Unit)? = null
    ) {
        init(context)
        updateLanguage()

        val textToSpeak = if (!customBlessingText.isNullOrBlank()) {
            customBlessingText
        } else {
            val cards = ReligionRepository.getCardsByLanguage(GlobalState.selectedLanguageCode)
            val selectedId = GlobalState.selectedDeityId
            val card = cards.find { it.id == selectedId }
            if (card != null) {
                context.applicationContext.getString(card.descResId)
            } else {
                GlobalState.selectedBlessingText ?: context.applicationContext.getString(R.string.msg_publish_success_default)
            }
        }

        val effectiveLoc = LocationFlowManager.getEffectiveLocation(latitude, longitude)
        val userLat = effectiveLoc.first
        val userLng = effectiveLoc.second

        ttsManager?.speakBlessingAndSequenceBackendBroadcasts(
            blessingText = textToSpeak,
            latitude = userLat,
            longitude = userLng,
            radiusKm = radiusKm,
            categories = categories,
            onBackendResponse = onBackendResponse,
            onSequenceComplete = onSequenceComplete
        )
    }

    /**
     * 追加後端連結請求發回之封包撥報序至續撥佇列中 (等待心靈祝福辭撥完自動續撥，撥完觸發序接播放完成)
     */
    fun enqueueBackendPacketTexts(
        context: Context,
        texts: List<String>,
        onSequenceComplete: (() -> Unit)? = null
    ) {
        init(context)
        ttsManager?.enqueueBackendPacketTexts(texts, onSequenceComplete)
    }

    fun shutdown() {
        ttsManager?.shutdown()
        ttsManager = null
    }
}
