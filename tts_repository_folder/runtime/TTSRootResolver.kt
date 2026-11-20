package com.example.tts.runtime

import com.example.tts.config.TTSConfig
import java.io.File
import java.nio.file.Files

/**
 * Configurable TTS root-path resolver for tts_repository_folder/.
 * Enforces strict path security and anti-traversal rules.
 */
class TTSRootResolver(private val customRootPath: File? = null) {

    fun resolveRoot(): File {
        val root = customRootPath ?: File(System.getProperty("user.dir") ?: ".", TTSConfig.ROOT_DIR_NAME)
        return root.canonicalFile
    }

    /**
     * Validates and resolves a relative or child path against the TTS root.
     * Rejects "../", absolute path escapes, and symbolic link escapes.
     */
    fun resolveChildPath(relativePath: String): File {
        val root = resolveRoot()

        val trimmed = relativePath.trim()
        if (trimmed.contains("..") || trimmed.startsWith("/") || trimmed.startsWith("\\") || trimmed.contains(":")) {
            throw SecurityException("Invalid path or traversal attempt detected: $relativePath")
        }

        val targetFile = File(root, trimmed)
        val canonicalRoot = root.canonicalFile
        val canonicalTarget = targetFile.canonicalFile

        val rootPathStr = canonicalRoot.absolutePath
        val targetPathStr = canonicalTarget.absolutePath

        if (!targetPathStr.startsWith(rootPathStr) || (targetPathStr.length > rootPathStr.length && targetPathStr[rootPathStr.length] != File.separatorChar)) {
            throw SecurityException("Access denied: path escapes TTS root -> $relativePath")
        }

        if (Files.isSymbolicLink(targetFile.toPath()) || Files.isSymbolicLink(canonicalTarget.toPath())) {
            val realTarget = targetFile.toPath().toRealPath()
            val realRoot = canonicalRoot.toPath()
            if (!realTarget.startsWith(realRoot)) {
                throw SecurityException("Symbolic link escape detected -> $relativePath")
            }
        }

        return canonicalTarget
    }
}
