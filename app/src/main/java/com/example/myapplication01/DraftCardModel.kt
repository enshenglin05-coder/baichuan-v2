package com.example.myapplication01

import com.google.gson.annotations.SerializedName

/**
 * 動作協定列舉 (4通訊與導航協議獨立對齊)
 */
enum class CardActionType {
    @SerializedName("LINE")
    LINE,

    @SerializedName("PHONE")
    PHONE,

    @SerializedName("MAP")
    MAP,

    @SerializedName("INTRO")
    INTRO,

    @SerializedName("WECHAT")
    WECHAT,

    @SerializedName("TELEGRAM")
    TELEGRAM,
}

/**
 * 卡片動作協定模型 (Card Action)
 */
data class CardAction(
    @SerializedName("action_type")
    val action_type: CardActionType = CardActionType.LINE,

    @SerializedName("value")
    val value: String = "",

    @SerializedName("uri")
    val uri: String = "",
)

/**
 * 卡片聯絡資訊模型 (Card Contact)
 */
data class CardContact(
    @SerializedName("method")
    val method: String = "LINE",

    @SerializedName("label")
    val label: String = "LINE ID",

    @SerializedName("val")
    val valField: String = "",

    @SerializedName("display")
    val display: String = "💬 聯絡對接",

    @SerializedName("link")
    val link: String = "",

    @SerializedName("phone")
    val phone: String = "",

    @SerializedName("phone_display")
    val phone_display: String = "",

    @SerializedName("intro_url")
    val intro_url: String = "",
)

/**
 * 垂直重向卡片草稿資料模型 (Draft Card Model)
 * 寫死底層標準 JSON 打包架構，絕對不可變更
 */
data class DraftCardModel(
    @SerializedName("idx")
    val idx: String = "#01",

    @SerializedName("card_index")
    val card_index: Int = 1,

    @SerializedName("id")
    val id: String = "",

    @SerializedName("did")
    val did: String = "",

    @SerializedName("t")
    val t: String = "",

    @SerializedName("dist")
    val dist: String = "0 km",

    @SerializedName("lat")
    val lat: Double = 0.0,

    @SerializedName("lng")
    val lng: Double = 0.0,

    @SerializedName("time")
    val time: String = "",

    @SerializedName("desc")
    val desc: String = "",

    @SerializedName("actions")
    val actions: List<CardAction> = emptyList(),

    @SerializedName("contact")
    val contact: CardContact = CardContact(),
) {
    companion object {
        /**
         * 將平台 RequirementCard 轉換為寫死底層標準 DraftCardModel
         */
        fun fromRequirementCard(card: RequirementCard, index: Int = 1): DraftCardModel {
            val idxStr = "#${index.toString().padStart(2, '0')}"
            val cleanVal = ContactUtils.cleanContactValue(card.contact_value)
            val lineUri = when (card.contact_method) {
                RequirementCard.ContactMethod.LINE -> if (cleanVal.isNotBlank()) "https://line.me/ti/p/~$cleanVal" else ""
                RequirementCard.ContactMethod.TELEGRAM -> if (cleanVal.isNotBlank()) "https://t.me/$cleanVal" else ""
                RequirementCard.ContactMethod.WECHAT -> if (cleanVal.isNotBlank()) "weixin://dl/chat?$cleanVal" else ""
                else -> if (cleanVal.isNotBlank()) "https://line.me/ti/p/~$cleanVal" else ""
            }
            val rawPhone = card.getPhoneForDialing() ?: ""
            val phoneUri = if (rawPhone.isNotBlank()) "tel:$rawPhone" else ""
            val mapUri = "https://www.google.com/maps/search/?api=1&query=${card.gps_lat},${card.gps_lng}"
            val introUri = card.media_url?.ifBlank { null } ?: ""

            val actionList = listOf(
                CardAction(CardActionType.LINE, value = cleanVal, uri = lineUri),
                CardAction(CardActionType.PHONE, value = rawPhone, uri = phoneUri),
                CardAction(CardActionType.MAP, value = "${card.gps_lat},${card.gps_lng}", uri = mapUri),
                CardAction(CardActionType.INTRO, value = card.media_url ?: "", uri = introUri)
            )

            val contactInfo = CardContact(
                method = card.contact_method.name,
                label = "${card.contact_method.name} ID",
                valField = cleanVal,
                display = "💬 聯絡對接 (${card.contact_method.name}: $cleanVal)",
                link = lineUri,
                phone = rawPhone,
                phone_display = if (rawPhone.isNotBlank()) "📞 撥打電話 ($rawPhone)" else "📞 撥打電話",
                intro_url = introUri
            )

            return DraftCardModel(
                idx = idxStr,
                card_index = index,
                id = card.card_id,
                did = card.publisher_device_id,
                t = card.category_tag.ifBlank { "需求卡片" },
                dist = "周邊",
                lat = card.gps_lat,
                lng = card.gps_lng,
                time = "12小時內",
                desc = card.description,
                actions = actionList,
                contact = contactInfo,
            )
        }
    }
}
