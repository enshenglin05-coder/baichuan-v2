package com.example.tts

import com.google.gson.annotations.SerializedName

/**
 * Data model for ui5_manual_packet_*.json files, matching original structure without changes.
 */
data class TtsPacketModel(
    @SerializedName("packetId", alternate = ["id", "cardId", "card_id"])
    val packetId: String = "",

    @SerializedName("packetCode", alternate = ["packet_code", "folderCode", "folder_code", "code7"])
    val packetCode: String = "",

    @SerializedName("text", alternate = ["description", "speechContent", "content"])
    val text: String = "",

    @SerializedName("remainingCount", alternate = ["remaining_count", "remainingQuota", "remaining_quota"])
    val remainingCount: Int = 100,

    @SerializedName("timestamp", alternate = ["createdAt", "created_at", "time"])
    val timestamp: String = "",

    @SerializedName("latitude", alternate = ["lat", "gps_lat"])
    val latitude: Double = 0.0,

    @SerializedName("longitude", alternate = ["lng", "gps_lng"])
    val longitude: Double = 0.0,

    @SerializedName("radiusKm", alternate = ["radius_km", "radius"])
    val radiusKm: Double = 6.0,

    @SerializedName("isArchived", alternate = ["is_archived", "archived"])
    val isArchived: Boolean = false
)
