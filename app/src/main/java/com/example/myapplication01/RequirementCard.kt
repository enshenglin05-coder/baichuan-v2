package com.example.myapplication01

import com.google.gson.annotations.SerializedName

/**
 * 百川 1.1 統一外殼模板 (Shell Envelope)
 * 核心編號矩陣 (11x8) 與模組化邏輯
 */
data class RequirementCard(
    @SerializedName(value = "card_id", alternate = ["cardId", "id"]) val card_id: String = "",
    @SerializedName(value = "publisher_device_id", alternate = ["publisherDeviceId", "did"]) val publisher_device_id: String = "",
    @SerializedName(value = "created_at", alternate = ["timestamp", "createdAt", "time"]) val created_at: Long = System.currentTimeMillis(),
    
    // 按讚機制 (1.0 擴展)
    @SerializedName("like_count") val like_count: Int = 0,
    @SerializedName("dislike_count") val dislike_count: Int = 0,
    
    // 11x8 矩陣編碼 (如 "1-1" 到 "11-8")
    @SerializedName(value = "category_code", alternate = ["drawer_code", "drawerCode"]) val category_code: String = "1-1",
    @SerializedName(value = "drawer_type", alternate = ["type"]) val drawer_type: String = "1A", // 1A: 需求, 1B: 供給
    
    @SerializedName(value = "category_tag", alternate = ["cardType", "category_name", "categoryName"]) val category_tag: String = "",
    @SerializedName("description", alternate = ["desc"]) val description: String = "",
    @SerializedName(value = "media_url", alternate = ["personalIntroUrl", "intro_url"]) val media_url: String? = null,
    
    // 4 選 1 聯絡工具模組化支援
    @SerializedName("contact_method", alternate = ["contact_type", "contactType"]) val contact_method: ContactMethod = ContactMethod.LINE,
    @SerializedName(value = "contact_value", alternate = ["lineId", "contact_id", "contactId"]) val contact_value: String = "",
    @SerializedName(value = "phone_number", alternate = ["phone", "phoneNumber", "mobile", "tel"]) val phone_number: String? = null,
    
    // 經緯度定位與距離計算核心
    @SerializedName(value = "gps_lat", alternate = ["latitude", "lat"]) val gps_lat: Double = 0.0,
    @SerializedName(value = "gps_lng", alternate = ["longitude", "lng"]) val gps_lng: Double = 0.0,
    @SerializedName("mapUrl") val mapUrl: String = "https://www.google.com/maps/search/?api=1&query=$gps_lat,$gps_lng",
    
    @SerializedName("theme_color") val theme_color: String = "#FFFFFF",
    
    // 風控與 12 小時過期邏輯
    @SerializedName("moderation_status") val moderation_status: String = "NORMAL",
    @SerializedName("reporter_ids") val reporter_ids: Set<String> = emptySet(),
    @SerializedName("reports_by_reason") val reports_by_reason: Map<String, Set<String>> = emptyMap(),
    @SerializedName("accepted_by_device_id") val accepted_by_device_id: String? = null,
    
    @SerializedName(value = "reportCount", alternate = ["report_count"]) val reportCount: Int = reporter_ids.size,
    @SerializedName(value = "errorCount", alternate = ["error_count"]) val errorCount: Int = 0,
    
    // TTL 與測試模式支援
    @SerializedName("ttl_hours") val ttl_hours: Int = 12,
    @SerializedName("is_test_only") val is_test_only: Boolean = false,
    
    @Transient val isSelfOwner: Boolean = false
) {
    val phone: String? get() = phone_number

    enum class ContactMethod {
        @SerializedName("LINE") LINE,
        @SerializedName("TELEGRAM") TELEGRAM,
        @SerializedName("WECHAT") WECHAT,
        @SerializedName("PHONE") PHONE
    }

    /**
     * 獲取用於撥號的有效電話號碼 (針對真實卡片與預設卡片統一提煉)
     */
    fun getPhoneForDialing(): String? {
        // 1. 真實雲端 JSON 卡片：優先正確讀取 card.phone / phone_number
        val realPhone = phone?.trim()?.takeIf { it.isNotBlank() && it != "null" }
            ?: phone_number?.trim()?.takeIf { it.isNotBlank() && it != "null" }
        if (!realPhone.isNullOrEmpty()) {
            val cleaned = PhoneActionHelper.cleanPhoneNumber(realPhone)
            return if (cleaned.isNotBlank()) cleaned else realPhone
        }

        // 2. 預設卡片 / 其它來源檢查 (若沒有實質電話資料，後續交由防呆機制顯示 Toast)
        val cv = contact_value.trim()
        if (cv.isNotEmpty() && cv != "null") {
            if (contact_method == ContactMethod.PHONE) {
                val cleaned = PhoneActionHelper.cleanPhoneNumber(cv)
                if (cleaned.isNotBlank()) return cleaned
            }
            val extracted = PhoneActionHelper.extractPhoneNumber(cv)
            if (!extracted.isNullOrBlank()) {
                return extracted
            }
        }

        // 3. 從說明 (description) 中提煉電話號碼
        val extractedDesc = PhoneActionHelper.extractPhoneNumber(description)
        if (!extractedDesc.isNullOrBlank()) {
            return extractedDesc
        }

        return null
    }

    /**
     * 動態 12 小時 TTL 效期檢查 (無論真實或虛擬卡片，暫存期限一律為 12 小時，超過即抹除)
     */
    fun isExpired(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val effectiveTtlHours = if (ttl_hours in 1..12) ttl_hours else 12
        val ttlMillis = effectiveTtlHours * 60 * 60 * 1000L
        return (nowMillis - created_at) > ttlMillis
    }

    /**
     * 是否為預留擴充池 (Category 11)
     */
    fun isReserved(): Boolean = category_code.startsWith("11-")

    /**
     * 判斷是否為預設/靜態測試卡片
     * 用於隔開預設卡片與發送者真實卡片之 Google Map 經緯度導航與渲染邏輯
     */
    fun isPresetCard(): Boolean {
        return is_test_only || 
               publisher_device_id == "SEARCH_ENGINE_AUTO_NODE" || 
               publisher_device_id == "SYSTEM_ADMIN" || 
               card_id.startsWith("TEST_") || 
               card_id.startsWith("SEARCH_AUTO_CARD_")
    }
}
