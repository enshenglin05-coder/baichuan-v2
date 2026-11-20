package com.example.tts.runtime

import com.example.tts.commands.TTSCommandDispatcher

/**
 * Independent runtime entry point for the TTS mini root.
 * Initializes core components without autonomous business logic execution.
 */
class TTSRuntime(
    val rootResolver: TTSRootResolver = TTSRootResolver(),
    val reader: TTSReader = TTSReader(rootResolver),
    val commandDispatcher: TTSCommandDispatcher = TTSCommandDispatcher()
) {
    init {
        val root = rootResolver.resolveRoot()
        if (!root.exists()) {
            root.mkdirs()
        }
    }

    fun isHealthy(): Boolean {
        return reader.checkRootExists()
    }
}
