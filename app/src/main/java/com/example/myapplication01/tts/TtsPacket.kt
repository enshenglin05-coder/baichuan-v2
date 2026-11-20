package com.example.myapplication01.tts

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.util.Locale

/**
 * Data class representing a JSON packet file in the TTS root directory.
 *
 * Enforces strict UI5-originated identity and genuine NULL values.
 * Missing or unreadable latitude, longitude, radius, or packetId are NULL without synthetic defaults.
 */
data class TtsPacket(
    @SerializedName("packetId", alternate = ["id", "cardId", "card_id"])
    val packetId: String? = null,

    @SerializedName("packetCode", alternate = ["packet_code", "folderCode", "folder_code", "code7"])
    val packetCode: String? = null,

    @SerializedName("text", alternate = ["description", "speechContent", "content"])
    val text: String? = null,

    @SerializedName("remainingCount", alternate = ["remaining_count", "remainingQuota", "remaining_quota"])
    var remainingCount: Int = 100,

    @SerializedName("timestamp", alternate = ["createdAt", "created_at", "time"])
    val timestampStr: String? = null,

    @Transient
    val timestamp: Long = 0L,

    @SerializedName("latitude", alternate = ["lat", "gps_lat"])
    val latitude: Double? = null,

    @SerializedName("longitude", alternate = ["lng", "gps_lng"])
    val longitude: Double? = null,

    @SerializedName("radiusKm", alternate = ["radius_km", "radius"])
    val radiusKm: Double? = null,

    @SerializedName("formulaIdentifier", alternate = ["formula_identifier", "formulaId", "formula_id"])
    val formulaIdentifier: String? = "HAVERSINE_SPHERICAL",

    @SerializedName("formulaVersion", alternate = ["formula_version", "formulaVer", "formula_ver"])
    val formulaVersion: String? = "1.0.0",

    @SerializedName("geofenceType", alternate = ["geofence_type", "geofence"])
    val geofenceType: String? = "RADIAL_CIRCLE",

    @SerializedName("geofenceParams", alternate = ["geofence_params", "geofence_parameters"])
    val geofenceParams: Map<String, Any>? = null,

    @Transient
    var file: File? = null
) {
    /**
     * Validates that packet originates from a valid UI5 record and has no missing/NULL mandatory fields.
     */
    fun isDataValid(): Boolean {
        if (packetId.isNullOrBlank()) return false
        if (latitude == null || latitude.isNaN() || latitude.isInfinite()) return false
        if (longitude == null || longitude.isNaN() || longitude.isInfinite()) return false
        if (radiusKm == null || radiusKm.isNaN() || radiusKm.isInfinite() || radiusKm <= 0.0) return false
        if (text.isNullOrBlank()) return false
        return true
    }

    fun parseTimestampMillis(): Long {
        if (timestamp > 0L) return timestamp
        if (timestampStr.isNullOrBlank()) return file?.lastModified() ?: 0L
        timestampStr.toLongOrNull()?.let { return it }
        return com.example.myapplication01.TtsRepositoryManager.parseTimestampToMillis(timestampStr)
    }

    /**
     * Calculates the sum of digits of the 7-digit packet code / filename code.
     * E.g., "1234567" -> 1+2+3+4+5+6+7 = 28
     */
    fun get7DigitSum(): Int {
        val targetStr = when {
            !packetCode.isNullOrBlank() -> packetCode
            packetId != null && packetId.contains("ui5_pkt_") -> packetId.substringAfter("ui5_pkt_").substringBefore("_")
            file != null -> file!!.nameWithoutExtension
            else -> packetId ?: ""
        }
        val digits = targetStr.filter { it.isDigit() }
        if (digits.isEmpty()) return 999
        val first7 = if (digits.length >= 7) digits.substring(0, 7) else digits
        return first7.sumOf { it.digitToInt() }
    }

    /**
     * Extracts the last 3 digits of latitude for fast candidate matching if latitude is not null.
     */
    fun getLast3DigitsLat(): Int? {
        val lat = latitude ?: return null
        return extractLast3Digits(lat)
    }

    /**
     * Extracts the last 3 digits of longitude for fast candidate matching if longitude is not null.
     */
    fun getLast3DigitsLng(): Int? {
        val lng = longitude ?: return null
        return extractLast3Digits(lng)
    }

    fun toJsonString(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun extractLast3Digits(coord: Double?): Int? {
            if (coord == null || coord.isNaN() || coord.isInfinite()) return null
            val str = String.format(Locale.US, "%.6f", kotlin.math.abs(coord)).replace(".", "")
            val digitsOnly = str.filter { it.isDigit() }
            return if (digitsOnly.length >= 3) {
                digitsOnly.takeLast(3).toIntOrNull()
            } else {
                digitsOnly.toIntOrNull()
            }
        }
    }
}
