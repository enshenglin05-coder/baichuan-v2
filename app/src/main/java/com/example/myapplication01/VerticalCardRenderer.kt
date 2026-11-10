package com.example.myapplication01

/**
 * 垂直縱向卡片渲染器 (Vertical Card Renderer for Draft & 96-Grid Outline)
 * 專工「一鍵打包」與 JSON 卡片動態對話框渲染邏輯：
 * 1. 介面維持垂直縱向（Vertical Stack）、一張一張卡片的清單佈局
 * 2. 清楚呈現：服務/需求內容、個人/產品介紹、電話、地圖導航、通訊對接等區塊
 * 3. 四個通訊與導航按鈕（LINE, 電話, 地圖, 個人/產品介紹）各自獨立垂直排列、協議正確
 */
object VerticalCardRenderer {

    /**
     * 渲染單張垂直縱向卡片與對應區塊內容
     */
    fun renderVerticalDraftCard(card: DraftCardModel): String {
        // 嚴格確保四通訊協定各自對應正確編碼與網址
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
        val introText = if (introLink != "#" && introLink.isNotBlank()) introLink else "尚未提供介紹連結"

        return """
            <div class="vertical-card-item" data-index="${card.idx}">
              <!-- 卡片識別標頭 -->
              <div class="card-header">
                <span class="card-idx">${card.idx}</span>
                <h3 class="card-title">${card.t} (${card.dist})</h3>
                <span class="card-time">${card.time}</span>
              </div>

              <!-- 區塊 1：📌 服務 / 需求內容 -->
              <div class="card-section">
                <div class="section-label">📌 服務 / 需求內容</div>
                <p class="section-content">${card.desc}</p>
              </div>

              <!-- 區塊 2：🔗 個人 / 產品介紹 -->
              <div class="card-section">
                <div class="section-label">🔗 個人 / 產品介紹</div>
                <p class="section-content">$introText</p>
              </div>

              <!-- 區塊 3：📞 電話與對接資訊 -->
              <div class="card-section">
                <div class="section-label">📞 電話與通訊對接</div>
                <p class="section-content">${if (card.contact.phone.isNotBlank()) "電話: " + card.contact.phone else "電話: 未提供"} | $contactDisplay</p>
              </div>

              <!-- 區塊 4：垂直縱向按鈕清單 (Vertical Stack Action Buttons) -->
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
     * 生成 96 格大綱之垂直縱向 HTML 區塊
     */
    fun render96GridOutlineHtml(context: android.content.Context? = null): String {
        val sb = java.lang.StringBuilder()
        sb.append("""
            <div class="outline-container">
              <div class="outline-main-title">📋 百川 12大項 96格網格對齊大綱與引導</div>
              <div class="outline-grid-list">
        """.trimIndent())

        for (main in 1..Grid88Formatter.TOTAL_MAIN_CATEGORIES) {
            val mainName = when (main) {
                1 -> "1. 食 (餐飲美食)"
                2 -> "2. 衣 (潮流服飾)"
                3 -> "3. 住 (租屋裝潢)"
                4 -> "4. 行 (交通跑腿)"
                5 -> "5. 教育 (托育學習)"
                6 -> "6. 娛樂 (休閒娛樂)"
                7 -> "7. 醫療 (健康照護)"
                8 -> "8. 二手 (閒置 3C)"
                9 -> "9. 寵物 (寵物服務)"
                10 -> "10. 服務 (專業短工)"
                11 -> "11. 公益 (公益關懷)"
                12 -> "12. 信仰 (信仰祈福)"
                else -> "$main. 綜合項目"
            }

            sb.append("""
                <div class="outline-category-block">
                  <div class="outline-category-name">$mainName</div>
            """.trimIndent())

            val subItems = CategoryRepository.getSubItems(main.toString())
            for (sub in 1..Grid88Formatter.SUB_CATEGORIES_PER_MAIN) {
                val gridCode = "$main-$sub"
                val subName = if (sub - 1 in subItems.indices) {
                    CategoryRepository.getCategoryTitle(gridCode, context)
                } else {
                    "格子 $gridCode"
                }
                sb.append("""
                  <div class="outline-sub-item">• [$gridCode] $subName</div>
                """.trimIndent())
            }
            sb.append("</div>\n")
        }

        sb.append("""
              </div>
            </div>
        """.trimIndent())

        return sb.toString()
    }

    /**
     * 批量載入 JSON 卡片並生成垂直縱向清單
     */
    fun generateDraftCardList(cardsJson: List<DraftCardModel>): String {
        return cardsJson.joinToString("\n") { renderVerticalDraftCard(it) }
    }

    /**
     * 包裹完整 HTML5 文件結構與 CSS 樣式供 WebView 直接加載 (垂直縱向 Vertical Stack 排版)
     */
    fun wrapHtmlDocument(cardsHtml: String, outlineHtml: String = "", cardCount: Int = 0): String {
        return """
            <!DOCTYPE html>
            <html lang="zh-TW">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>一鍵打包 垂直縱向檢視</title>
              <style>
                * { box-sizing: border-box; }
                body {
                  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                  background-color: #121212;
                  color: #E0E0E0;
                  padding: 12px;
                  margin: 0;
                  line-height: 1.5;
                }
                .pack-header-title {
                  color: #00FFC2;
                  font-size: 18px;
                  font-weight: bold;
                  text-align: center;
                  margin: 8px 0 16px 0;
                  padding-bottom: 8px;
                  border-bottom: 2px solid #00E5FF;
                }
                .outline-container {
                  background: #181824;
                  border: 1px solid #2A2A3D;
                  border-radius: 12px;
                  padding: 14px;
                  margin-bottom: 16px;
                }
                .outline-main-title {
                  color: #00E5FF;
                  font-size: 15px;
                  font-weight: bold;
                  margin-bottom: 12px;
                  border-bottom: 1px solid #2A2A3D;
                  padding-bottom: 6px;
                }
                .outline-category-block {
                  background: #202030;
                  border-radius: 8px;
                  padding: 10px;
                  margin-bottom: 10px;
                }
                .outline-category-name {
                  color: #FFD700;
                  font-size: 14px;
                  font-weight: bold;
                  margin-bottom: 6px;
                }
                .outline-sub-item {
                  font-size: 13px;
                  color: #CCCCCC;
                  padding: 3px 0 3px 8px;
                  border-left: 2px solid #00E5FF;
                  margin-bottom: 4px;
                }
                .divider-title {
                  background: #2A2A3D;
                  color: #00E5FF;
                  padding: 8px 12px;
                  border-radius: 6px;
                  font-size: 14px;
                  font-weight: bold;
                  text-align: center;
                  margin: 16px 0;
                }
                .vertical-card-list {
                  display: flex;
                  flex-direction: column;
                  gap: 16px;
                }
                .vertical-card-item {
                  background: #1E1E2E;
                  border: 1px solid #2B2B3D;
                  border-radius: 12px;
                  padding: 16px;
                  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                  display: flex;
                  flex-direction: column;
                  gap: 12px;
                }
                .card-header {
                  display: flex;
                  align-items: center;
                  justify-content: space-between;
                  border-bottom: 1px solid #2B2B3D;
                  padding-bottom: 8px;
                }
                .card-idx {
                  background: #00E5FF;
                  color: #000000;
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
                .card-section {
                  background: #141420;
                  border-radius: 8px;
                  padding: 10px;
                  border-left: 3px solid #00FFC2;
                }
                .section-label {
                  font-size: 13px;
                  font-weight: bold;
                  color: #00FFC2;
                  margin-bottom: 4px;
                }
                .section-content {
                  font-size: 14px;
                  color: #DDDDDD;
                  margin: 0;
                  word-break: break-all;
                }
                .card-actions-vertical {
                  display: flex;
                  flex-direction: column;
                  gap: 8px;
                  margin-top: 4px;
                }
                .btn-action {
                  display: block;
                  width: 100%;
                  text-align: center;
                  padding: 11px 16px;
                  border-radius: 8px;
                  text-decoration: none;
                  font-weight: bold;
                  font-size: 14px;
                  box-sizing: border-box;
                }
                .line-action { background: #00B900; color: #FFFFFF; }
                .phone-action { background: #FF9800; color: #FFFFFF; }
                .map-action { background: #1A73E8; color: #FFFFFF; }
                .intro-action { background: #9C27B0; color: #FFFFFF; }
              </style>
            </head>
            <body>
              <div class="pack-header-title">📋 一鍵打包 垂直縱向檢視 (共 $cardCount 筆)</div>
              $outlineHtml
              <div class="divider-title">=== [目標卡片與一鍵打包草稿 (共 $cardCount 筆)] ===</div>
              <div class="vertical-card-list">
                $cardsHtml
              </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 帶有 96 格大綱與目標卡片之垂直縱向打包渲染入口
     */
    fun packageAndRenderJsonWithOutline(context: android.content.Context?, cards: List<DraftCardModel>): String {
        val outlineHtml = render96GridOutlineHtml(context)
        val cardsHtml = generateDraftCardList(cards)
        return wrapHtmlDocument(cardsHtml, outlineHtml, cards.size)
    }

    /**
     * 一鍵打包並渲染 JSON 格式卡片資料之標準入口
     */
    fun packageAndRenderJson(cards: List<DraftCardModel>): String {
        return packageAndRenderJsonWithOutline(null, cards)
    }
}

