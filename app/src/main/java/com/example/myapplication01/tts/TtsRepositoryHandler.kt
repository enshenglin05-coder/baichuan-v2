package com.example.myapplication01.tts

import android.content.Context
import com.example.myapplication01.BaichuanApiService

/**
 * Repository handler for packet countdown and storage push.
 * Preserves original UI5 packet identity and existing permissions.
 */
class TtsRepositoryHandler(val context: Context?) {

    fun processPacketCountdown(packet: TtsPacket, onCompleted: (Boolean) -> Unit) {
        val pktId = packet.packetId
        if (pktId.isNullOrBlank() || !packet.isDataValid()) {
            removeActiveLocalFile(packet)
            onCompleted(true)
            return
        }

        packet.remainingCount -= 1

        if (packet.remainingCount <= 0) {
            val jsonBody = packet.toJsonString()

            BaichuanApiService.deleteActiveTtsPacketAndPushToArchive(
                packetId = pktId,
                zeroedJsonBodyStr = jsonBody,
                onSuccess = {
                    removeActiveLocalFile(packet)
                    onCompleted(true)
                },
                onError = { _ ->
                    removeActiveLocalFile(packet)
                    onCompleted(true)
                }
            )
        } else {
            updateLocalFile(packet)
            onCompleted(false)
        }
    }

    private fun removeActiveLocalFile(packet: TtsPacket) {
        try {
            val file = packet.file
            if (file != null && file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
    }

    private fun updateLocalFile(packet: TtsPacket) {
        try {
            val file = packet.file
            if (file != null) {
                file.writeText(packet.toJsonString(), Charsets.UTF_8)
            }
        } catch (_: Exception) {
        }
    }
}
