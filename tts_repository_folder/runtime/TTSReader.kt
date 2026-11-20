package com.example.tts.runtime

import com.example.tts.config.TTSConfig
import java.io.File
import java.io.FileNotFoundException

/**
 * Dedicated isolated TTS reader.
 * Connects to explicitly permitted TTS child directories and reads permitted files securely.
 */
class TTSReader(private val rootResolver: TTSRootResolver = TTSRootResolver()) {

    fun checkRootExists(): Boolean {
        val root = rootResolver.resolveRoot()
        return root.exists() && root.isDirectory
    }

    /**
     * Discovers files within permitted subdirectories of the TTS root.
     */
    fun discoverFiles(subDirectory: String = ""): List<File> {
        val targetDir = if (subDirectory.isBlank()) {
            rootResolver.resolveRoot()
        } else {
            val topDir = subDirectory.substringBefore('/')
            if (!TTSConfig.isPermittedDirectory(topDir)) {
                throw SecurityException("Access denied: directory $topDir is not in permitted child directories.")
            }
            rootResolver.resolveChildPath(subDirectory)
        }

        if (!targetDir.exists()) {
            throw FileNotFoundException("Directory does not exist: ${targetDir.absolutePath}")
        }
        if (!targetDir.isDirectory) {
            throw IllegalArgumentException("Target is not a directory: ${targetDir.absolutePath}")
        }

        val files = mutableListOf<File>()
        walkSecurely(targetDir, rootResolver.resolveRoot(), files)
        return files.sortedBy { it.name }
    }

    private fun walkSecurely(current: File, root: File, accumulator: MutableList<File>) {
        val canonicalRoot = root.canonicalFile
        val canonicalCurrent = current.canonicalFile

        if (!canonicalCurrent.absolutePath.startsWith(canonicalRoot.absolutePath)) {
            throw SecurityException("Traversal violation: outside TTS root")
        }

        val childFiles = canonicalCurrent.listFiles() ?: return
        for (file in childFiles) {
            val canonicalFile = file.canonicalFile
            if (!canonicalFile.absolutePath.startsWith(canonicalRoot.absolutePath)) {
                throw SecurityException("Traversal violation on file")
            }
            if (canonicalFile.isDirectory) {
                walkSecurely(canonicalFile, canonicalRoot, accumulator)
            } else if (canonicalFile.isFile) {
                accumulator.add(canonicalFile)
            }
        }
    }

    fun readFileContent(relativePath: String): String {
        val topDir = relativePath.substringBefore('/')
        if (topDir.isNotBlank() && TTSConfig.isPermittedDirectory(topDir)) {
            // permitted
        } else if (relativePath.contains("/")) {
            throw SecurityException("Access denied: path $topDir not permitted.")
        }
        val file = rootResolver.resolveChildPath(relativePath)
        if (!file.exists()) {
            throw FileNotFoundException("File not found: ${file.absolutePath}")
        }
        if (!file.isFile) {
            throw IllegalArgumentException("Target is not a file: ${file.absolutePath}")
        }
        return file.readText(Charsets.UTF_8)
    }
}
