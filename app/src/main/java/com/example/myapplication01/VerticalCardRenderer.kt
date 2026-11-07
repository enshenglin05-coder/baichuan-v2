package com.example.myapplication01

/**
 * 垂直重向卡片渲染器 (Vertical Card Renderer for Draft)
 * 寫死底層「一鍵打包」與 JSON 卡片動態渲染邏輯：
 * 1. 介面維持垂直重向、一張一張的清單佈局
 * 2. 四個通訊與導航按鈕（LINE, 電話, 地圖, 個人/產品介紹）各自獨立、協議正確、絕對不混淆
 */
object VerticalCardRenderer {

    /**
     * 渲染單張垂直卡片與四通訊跳轉邏輯
     */
    fun renderVerticalDraftCard(card: DraftCardModel): String {
        // 嚴格確保四通訊協議各自對應正確編碼
        val lineLink = card.actions.find { it.action_type == CardActionType.LINE }?.uri
            ?.ifBlank { null }
            ?: if (card.contact.valField.isNotBlank()) "https://line.me/ti/p/~${card.contact.valField}" else "#"

        val phoneLink = card.actions.find { it.action_type == CardActionType.PHONE }?.uri
            ?.ifBlank { null }
            ?: if (card.contact.phone.isNotBlank()) "tel:${card.contact.phone}" else "#"

        val mapLink = card.actions.find { it.action_type == CardActionType.MAP }?.uri
            ?.ifBlank { null }
            ?: "https://www.google.com/maps/search/?api=1&query=${card.lat},${card.lng}"

        val introLink = card.actions.find { it.action_type == CardActionType.INTRO }?.uri
            ?.ifBlank { null }
            ?: if (card.contact.intro_url.isNotBlank()) card.contact.intro_url else "#"

        val contactDisplay = card.contact.display.ifBlank { "💬 聯絡對接" }
        val phoneDisplay = if (card.contact.phone.isNotBlank()) "📞 撥打電話 (${card.contact.phone})" else "📞 撥打電話"
        val introDisplay = "🔗 個人 / 產品介紹"

        return """
            <div class="vertical-card-item" data-index="${card.idx}">
              <div class="card-header">
                <span class="card-idx">${card.idx}</span>
                <h3 class="card-title">${card.t} (${card.dist})</h3>
                <span class="card-time">${card.time}</span>
              </div>
              <div class="card-body">
                <p class="card-desc">${card.desc}</p>
              </div>
              <div class="card-actions-vertical">
                <!-- 1. 💬 社交通訊對接按鈕 (LINE/WeChat/Telegram) -->
                <a href="$lineLink" class="btn-action line-action" target="_blank">
                  $contactDisplay
                </a>
                <!-- 2. 📞 電話直接撥打按鈕 -->
                <a href="$phoneLink" class="btn-action phone-action">
                  $phoneDisplay
                </a>
                <!-- 3. 📍 地圖位置導航按鈕 -->
                <a href="$mapLink" class="btn-action map-action" target="_blank">
                  📍 地圖導航
                </a>
                <!-- 4. 🔗 個人 / 產品介紹按鈕 -->
                <a href="$introLink" class="btn-action intro-action" target="_blank">
                  $introDisplay
                </a>
              </div>
            </div>
        """.trimIndent()
    }

    /**
     * 批量載入 JSON 卡片並生成垂直清單
     */
    fun generateDraftCardList(cardsJson: List<DraftCardModel>): String {
        return cardsJson.joinToString("\n") { renderVerticalDraftCard(it) }
    }

    /**
     * 包裹完整 HTML5 文件結構與 CSS 樣式供 WebView 直接加載
     */
    fun wrapHtmlDocument(cardsHtml: String): String {
        return """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>垂直卡片清單</title>
              <style>
                body {
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                  background-color: #121212;
                  color: #E0E0E0;
                  padding: 12px;
                  margin: 0;
                }
                .vertical-card-item {
                  background: #1E1E2E;
                  border: 1px solid #2B2B3D;
                  border-radius: 12px;
                  padding: 16px;
                  margin-bottom: 16px;
                  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                }
                .card-header {
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  margin-bottom: 8px;
                }
                .card-idx {
                  background: #00E5FF;
                  color: #000;
                  font-weight: bold;
                  padding: 2px 8px;
                  border-radius: 4px;
                  font-size: 12px;
                }
                .card-title {
                  margin: 0 8px;
                  font-size: 16px;
                  color: #FFFFFF;
                  flex-grow: 1;
                }
                .card-time {
                  font-size: 12px;
                  color: #888888;
                }
                .card-body {
                  margin-bottom: 12px;
                }
                .card-desc {
                  font-size: 14px;
                  line-height: 1.5;
                  color: #CCCCCC;
                  margin: 0;
                }
                .card-actions-vertical {
                  display: flex;
                  flex-direction: column;
                  gap: 8px;
                }
                .btn-action {
                  display: block;
                  text-align: center;
                  padding: 10px 16px;
                  border-radius: 8px;
                  text-decoration: none;
                  font-weight: bold;
                  font-size: 14px;
                }
                .line-action { background: #00B900; color: #FFFFFF; }
                .phone-action { background: #FF9800; color: #FFFFFF; }
                .map-action { background: #1A73E8; color: #FFFFFF; }
                .intro-action { background: #9C27B0; color: #FFFFFF; }
              </style>
            </head>
            <body>
              $cardsHtml
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 一鍵打包並渲染 JSON 格式卡片資料之標準入口
     */
    fun packageAndRenderJson(cards: List<DraftCardModel>): String {
        val htmlList = generateDraftCardList(cards)
        return wrapHtmlDocument(htmlList)
    }
}
