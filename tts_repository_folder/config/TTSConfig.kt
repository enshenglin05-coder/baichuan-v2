package com.example.tts.config

/**
 * Configuration constants for the TTS mini root.
 * Defines permitted child directories and access constraints.
 */
object TTSConfig {
    const val ROOT_DIR_NAME = "tts_repository_folder"
    val PERMITTED_CHILD_DIRECTORIES = setOf("data", "config", "runtime", "commands", "src")

    fun isPermittedDirectory(dirName: String): Boolean {
        return PERMITTED_CHILD_DIRECTORIES.contains(dirName)
    }
}
