package com.example.myapplication01

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedList
import java.util.Locale
import java.util.Queue

/**
 * TtsPacketChainManager：本地 TTS 與後端連結請求發回封包、等待心靈祝福辭、撥完續撥與序接播放完成管理器
 *
 * 核心功能：
 * 1. 本地 TTS 語音播報 (Local TTS)
 * 2. 跟後端連結請求發回封包 (Backend connection request & packet report / sync)
 * 3. 撥報序與等待心靈祝福辭 (Broadcast sequence & waiting for mind/soul blessing message / 心靈祝福辭)
 * 4. 撥完續撥與序接播放完成回調 (Sequential playback chaining & completion callback)
 */
object TtsPacketChainManager {

    private val speechQueue: Queue<String> = LinkedList()
    private var isChainPlaying = false
    private var onSequenceComplete: (() -> Unit)? = null

    private val mainHandler: Handler? by lazy {
        try {
            Handler(Looper.getMainLooper())
        } catch (_: Throwable) {
            null
        }
    }

    private fun safeLogD(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    /**
     * 啟動後端連結請求、發回封包、等待心靈祝福辭並進行序接連續播放
     *
     * @param context Context
     * @param backendPayloadText 後端請求/發回封包的提示文字 (例如 "後端連結請求中，正在發回封包與資料同步...")
     * @param includeDeityBlessing 是否自動掛載心靈祝福辭 ("等待心靈祝福辭")
     * @param customAdditionalTexts 額外續撥的文字列表 (撥完續撥)
     * @param onComplete 序接播放完成回調 (序接播放完成)
     */
    fun startChainPlayback(
        context: Context?,
        backendPayloadText: String = "後端連結請求中，正在發回封包與資料同步...",
        includeDeityBlessing: Boolean = true,
        customAdditionalTexts: List<String> = emptyList(),
        onComplete: (() -> Unit)? = null
    ) {
        val appContext = context?.applicationContext
        if (appContext != null) {
            DeityBlessingTTS.init(appContext)
        }
        onSequenceComplete = onComplete

        synchronized(speechQueue) {
            speechQueue.clear()

            // 1. 後端連結請求發回封包
            if (backendPayloadText.isNotBlank()) {
                speechQueue.offer(backendPayloadText)
            }

            // 2. 等待心靈祝福辭 (Mind/Soul Blessing Message)
            if (includeDeityBlessing && appContext != null) {
                val blessingText = getHeartSoulBlessingText(appContext)
                if (blessingText.isNotBlank()) {
                    speechQueue.offer("心靈祝福辭：$blessingText")
                }
            }

            // 3. 撥完續撥的自訂附加文字
            for (text in customAdditionalTexts) {
                if (text.isNotBlank()) {
                    speechQueue.offer(text)
                }
            }
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        safeLogD("TtsPacketChain", "[$timestamp] 🔗 啟動序接播放佇列，共 ${speechQueue.size} 項任務等待播報")

        // 觸發後端連結請求（非同步發回封包與資料同步）
        triggerBackendPacketRequest()

        // 開始依序播放
        playNextInQueue(context)
    }

    private fun getHeartSoulBlessingText(context: Context): String {
        return if (!GlobalState.selectedBlessingText.isNullOrBlank()) {
            GlobalState.selectedBlessingText!!
        } else {
            val cards = ReligionRepository.getCardsByLanguage(GlobalState.selectedLanguageCode)
            val selectedId = GlobalState.selectedDeityId
            val card = cards.find { it.id == selectedId }
            if (card != null) {
                context.getString(card.descResId)
            } else {
                context.getString(R.string.religion_tw_general_desc)
            }
        }
    }

    private fun triggerBackendPacketRequest() {
        val lat = GlobalState.globalLatitude ?: HurlungchuangTestManager.TARGET_LATITUDE
        val lng = GlobalState.globalLongitude ?: HurlungchuangTestManager.TARGET_LONGITUDE
        
        safeLogD("TtsPacketChain", "🌐 正在發起後端連結請求與發回封包 (Lat: $lat, Lng: $lng)...")
        
        try {
            // 呼叫 BaichuanApiService 進行背景經緯度封包請求與資料同步
            BaichuanApiService.fetchNearbyCards(lat, lng, 50.0, emptyList()) { cloudCards ->
                val jsonCount = cloudCards.size
                val filenames = cloudCards.map { "${it.card_id}.json" }
                safeLogD("TtsPacketChain", "✅ [HTTP 成功 - 後端封包回應] 實際取得 JSON 數量: $jsonCount | 檔名列表: $filenames | 解析結果: 成功接收並同步 $jsonCount 筆雲端資料包")
            }
        } catch (e: Throwable) {
            safeLogD("TtsPacketChain", "⚠️ 後端連結請求呼叫 (單元測試環境: ${e.message})")
        }
    }

    private fun playNextInQueue(context: Context?) {
        synchronized(this) {
            if (speechQueue.isEmpty()) {
                isChainPlaying = false
                safeLogD("TtsPacketChain", "🏁 序接播放全部完成 (序接播放完成)")
                val action = {
                    onSequenceComplete?.invoke()
                    onSequenceComplete = null
                }
                if (mainHandler != null) {
                    mainHandler?.post(action)
                } else {
                    action()
                }
                return
            }

            isChainPlaying = true
            val nextText = speechQueue.poll() ?: return

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            safeLogD("TtsPacketChain", "[$timestamp] 🗣️ 正在續撥佇列文字: '$nextText' (剩餘佇列數: ${speechQueue.size})")

            // 執行本地 TTS 播報
            if (context != null) {
                DeityBlessingTTS.speak(nextText, context)
            }

            // 根據文字長度智慧計算預估朗讀時間 (每字約 300ms + 800ms 間隔緩衝，最小 1800ms)
            val estimatedDurationMs = (nextText.length * 300L + 800L).coerceAtLeast(1800L)
            
            val action = {
                // 撥完續撥：當前項目播報完成後，自動銜接下一項
                playNextInQueue(context)
            }
            if (mainHandler != null) {
                mainHandler?.postDelayed(action, estimatedDurationMs)
            } else {
                action()
            }
        }
    }

    /**
     * 強制中止序接播放
     */
    fun stopChain() {
        synchronized(speechQueue) {
            speechQueue.clear()
        }
        isChainPlaying = false
        onSequenceComplete = null
        safeLogD("TtsPacketChain", "⏹️ 序接播放已強制中止")
    }

    fun isPlaying(): Boolean = isChainPlaying
}
