package com.example.myapplication01.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Local TTS Player.
 * Converts valid JSON text to speech locally on device (no cloud audio).
 * Strictly forbids playback of empty or invalid packets.
 */
class TtsLocalPlayer(
    val context: Context,
    private val onFirstWordSpoken: (utteranceId: String) -> Unit,
    private val onPlaybackDone: (utteranceId: String) -> Unit,
    private val onPlaybackError: (utteranceId: String) -> Unit
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingTextToSpeak: Pair<String, String>? = null

    @Volatile
    var isPlaybackBusy: Boolean = false
        private set

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isPlaybackBusy = true
                    utteranceId?.let { onFirstWordSpoken(it) }
                }

                override fun onDone(utteranceId: String?) {
                    isPlaybackBusy = false
                    utteranceId?.let { onPlaybackDone(it) }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isPlaybackBusy = false
                    utteranceId?.let { onPlaybackError(it) }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    isPlaybackBusy = false
                    utteranceId?.let { onPlaybackError(it) }
                }
            })
            isInitialized = true
            pendingTextToSpeak?.let { (text, utteranceId) ->
                pendingTextToSpeak = null
                speakText(text, utteranceId)
            }
        } else {
            isInitialized = false
        }
    }

    fun speakText(text: String?, utteranceId: String?): Boolean {
        if (text.isNullOrBlank() || utteranceId.isNullOrBlank()) return false
        if (isPlaybackBusy) return false

        if (!isInitialized || tts == null) {
            pendingTextToSpeak = Pair(text, utteranceId)
            return false
        }

        isPlaybackBusy = true
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            isPlaybackBusy = false
            return false
        }
        return true
    }

    fun releaseAudioResources() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {
        } finally {
            tts = null
            isInitialized = false
            isPlaybackBusy = false
            pendingTextToSpeak = null
        }
    }
}
