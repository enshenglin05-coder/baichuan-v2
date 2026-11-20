package com.example.myapplication01.tts

import com.google.gson.Gson
import java.io.File

/**
 * Scanner for TTS Root Directory.
 *
 * Enforces strict data validation rules:
 * 1. Missing or unreadable GPS data = NULL. Reject packet if matching cannot be completed accurately.
 * 2. Scans ALL JSON files completely without early break.
 * 3. Rejects non-UI5, missing, synthetic, or unreadable packets.
 */
class TtsDirectoryScanner(private val ttsRootDir: File) {

    private val gson = Gson()

    data class CandidateResult(
        val packet: TtsPacket,
        val isFastMatch: Boolean
    )

    /**
     * Scans ALL JSON files in the TTS root directory.
     * Rejects packets with missing or unreadable GPS, ID, or radius data.
     */
    fun scanAllPackets(userLat: Double?, userLng: Double?): List<CandidateResult> {
        if (userLat == null || userLng == null || userLat.isNaN() || userLng.isNaN()) {
            // Cannot accurately complete GPS matching: reject all and do not fetch/play
            return emptyList()
        }

        if (!ttsRootDir.exists() || !ttsRootDir.isDirectory) {
            return emptyList()
        }

        val jsonFiles = ttsRootDir.listFiles { _, name ->
            name.endsWith(".json", ignoreCase = true)
        } ?: return emptyList()

        val candidates = mutableListOf<CandidateResult>()

        for (file in jsonFiles) {
            try {
                val content = file.readText(Charsets.UTF_8)
                val packet = parsePacket(content, file) ?: continue

                // Strict validation: Reject if packet lacks UI5 identity or mandatory valid GPS/radius
                if (!packet.isDataValid()) {
                    continue
                }

                if (packet.remainingCount <= 0) {
                    continue
                }

                candidates.add(CandidateResult(packet = packet, isFastMatch = false))
            } catch (_: Exception) {
                // Continue scanning without breaking
            }
        }

        return candidates
    }

    private fun parsePacket(jsonStr: String, file: File): TtsPacket? {
        return try {
            val packet = gson.fromJson(jsonStr, TtsPacket::class.java) ?: return null
            if (!packet.isDataValid()) return null

            packet.file = file
            val tsMs = if (packet.timestamp > 0) {
                packet.timestamp
            } else if (!packet.timestampStr.isNullOrBlank()) {
                packet.timestampStr.toLongOrNull() ?: 0L
            } else {
                file.lastModified()
            }

            val resolvedText = extractTextFromJson(jsonStr, packet.text ?: "")
            if (resolvedText.isBlank()) return null

            packet.copy(
                timestamp = tsMs,
                text = resolvedText,
                file = file
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun extractTextFromJson(jsonStr: String, fallbackText: String): String {
        if (fallbackText.isNotBlank()) return fallbackText.trim()
        return try {
            val jsonObj = com.google.gson.JsonParser.parseString(jsonStr).asJsonObject
            for (key in arrayOf("text", "description", "speechContent", "content", "message", "speech")) {
                if (jsonObj.has(key) && !jsonObj.get(key).isJsonNull) {
                    val valStr = jsonObj.get(key).asString
                    if (valStr.isNotBlank()) return valStr.trim()
                }
            }
            ""
        } catch (_: Exception) {
            fallbackText.trim()
        }
    }
}
