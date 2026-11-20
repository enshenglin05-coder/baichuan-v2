package com.example.tts

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileNotFoundException

/**
 * Dedicated isolated TTS directory reader.
 * Reads ui5_manual_packet_*.json files and discovers JSON files inside tts_repository_folder/ and permitted subdirectories.
 */
class TtsDirectoryReader(private val rootResolver: TtsRootResolver = TtsRootResolver()) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Checks if the root directory exists and is accessible.
     */
    fun checkRootExists(): Boolean {
        val root = rootResolver.resolveRoot()
        return root.exists() && root.isDirectory
    }

    /**
     * Lists and discovers all JSON files (specifically ui5_manual_packet_*.json or any .json) in the root or permitted child directory.
     */
    fun discoverJsonFiles(subDirectory: String = ""): List<File> {
        val targetDir = if (subDirectory.isBlank()) {
            rootResolver.resolveRoot()
        } else {
            rootResolver.resolveChildPath(subDirectory)
        }

        if (!targetDir.exists()) {
            throw FileNotFoundException("Directory does not exist: ${targetDir.absolutePath}")
        }
        if (!targetDir.isDirectory) {
            throw IllegalArgumentException("Target is not a directory: ${targetDir.absolutePath}")
        }

        val jsonFiles = mutableListOf<File>()
        walkDirectorySecurely(targetDir, rootResolver.resolveRoot(), jsonFiles)
        return jsonFiles.sortedBy { it.name }
    }

    private fun walkDirectorySecurely(current: File, root: File, accumulator: MutableList<File>) {
        val canonicalRoot = root.canonicalFile
        val canonicalCurrent = current.canonicalFile

        if (!canonicalCurrent.absolutePath.startsWith(canonicalRoot.absolutePath)) {
            throw SecurityException("Traversal violation: ${canonicalCurrent.absolutePath} is outside root ${canonicalRoot.absolutePath}")
        }

        val files = canonicalCurrent.listFiles() ?: return
        for (file in files) {
            val canonicalFile = file.canonicalFile
            if (!canonicalFile.absolutePath.startsWith(canonicalRoot.absolutePath)) {
                throw SecurityException("Traversal violation on file: ${canonicalFile.absolutePath}")
            }
            if (canonicalFile.isDirectory) {
                walkDirectorySecurely(canonicalFile, canonicalRoot, accumulator)
            } else if (canonicalFile.isFile && canonicalFile.name.endsWith(".json")) {
                accumulator.add(canonicalFile)
            }
        }
    }

    /**
     * Reads a specific ui5_manual_packet_*.json file (or json file) by relative path without modifying it.
     */
    fun readPacketFile(relativePath: String): TtsPacketModel {
        val file = rootResolver.resolveChildPath(relativePath)
        if (!file.exists()) {
            throw FileNotFoundException("TTS packet file not found: ${file.absolutePath}")
        }
        if (!file.isFile) {
            throw IllegalArgumentException("Target is not a file: ${file.absolutePath}")
        }
        val content = file.readText(Charsets.UTF_8)
        return gson.fromJson(content, TtsPacketModel::class.java)
    }

    /**
     * Reads raw text content of a packet file without modifying structure.
     */
    fun readRawPacketJson(relativePath: String): String {
        val file = rootResolver.resolveChildPath(relativePath)
        if (!file.exists()) {
            throw FileNotFoundException("TTS packet file not found: ${file.absolutePath}")
        }
        return file.readText(Charsets.UTF_8)
    }

    /**
     * Discovers specifically ui5_manual_packet_*.json files in root or permitted child directory.
     */
    fun discoverManualPackets(subDirectory: String = ""): List<File> {
        val allJson = discoverJsonFiles(subDirectory)
        return allJson.filter { it.name.startsWith("ui5_manual_packet_") && it.name.endsWith(".json") }
    }
}
