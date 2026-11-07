package com.example.myapplication01

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

object GlobalTtsPlayer : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var appContext: Context? = null
    private val pendingTexts = mutableListOf<String>()

    fun init(context: Context) {
        appContext = context.applicationContext
        if (tts == null) {
            tts = TextToSpeech(appContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.TAIWAN)
            isReady = (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED)
            Log.d("GlobalTts", "TTS 單例初始化結果: $isReady")
            if (isReady && pendingTexts.isNotEmpty()) {
                val copy = pendingTexts.toList()
                pendingTexts.clear()
                copy.forEach { speak(it) }
            }
        }
    }

    fun speak(text: String) {
        if (isReady && tts != null) {
            try {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GLOBAL_TTS_ID")
                Log.d("GlobalTts", "正在強力播報: $text")
            } catch (e: Exception) {
                Log.e("GlobalTts", "播報失敗: ${e.message}")
            }
        } else {
            Log.w("GlobalTts", "TTS 尚未準備好，強制重新初始化與快取播報...")
            pendingTexts.add(text)
            appContext?.let { ctx ->
                if (tts == null) {
                    tts = TextToSpeech(ctx.applicationContext, this)
                }
            }
        }
    }
}
