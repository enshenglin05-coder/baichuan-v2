package com.example.myapplication01.tts

import com.example.myapplication01.DistanceUtils

/**
 * Selector for single packet selection from candidates.
 *
 * Enforces Strict Fetch Authorization:
 * - Fetch ONLY packets that pass actual GPS coordinate and configured distance comparison.
 * - Missing or unreadable data = NULL -> reject packet.
 * - No match = no fetch. Never generate synthetic packets.
 */
class TtsPacketSelector {

    fun selectSinglePacket(
        candidates: List<TtsDirectoryScanner.CandidateResult>,
        userLat: Double?,
        userLng: Double?,
        configuredDistanceKm: Double? = null
    ): TtsPacket? {
        if (userLat == null || userLng == null || userLat.isNaN() || userLng.isNaN()) return null
        if (candidates.isEmpty()) return null

        // 1. Strict Geographic Distance Comparison: actualDistance <= triggerRadius
        val eligiblePackets = candidates.map { it.packet }.filter { packet ->
            if (!packet.isDataValid()) return@filter false

            val pktLat = packet.latitude ?: return@filter false
            val pktLng = packet.longitude ?: return@filter false
            val pktRadius = packet.radiusKm ?: return@filter false

            val actualDistance = DistanceUtils.calculateDistance(userLat, userLng, pktLat, pktLng)
            if (actualDistance.isNaN() || actualDistance.isInfinite()) return@filter false

            val maxAllowedRadius = if (configuredDistanceKm != null && configuredDistanceKm > 0.0) {
                if (pktRadius < configuredDistanceKm) pktRadius else configuredDistanceKm
            } else {
                pktRadius
            }
            actualDistance <= maxAllowedRadius
        }

        if (eligiblePackets.isEmpty()) return null
        if (eligiblePackets.size == 1) return eligiblePackets.first()

        // 2. Select priority packet: smallest 7-digit sum, then earliest timestamp
        return eligiblePackets.minWithOrNull(
            compareBy({ it.get7DigitSum() }, { it.timestamp })
        )
    }
}
