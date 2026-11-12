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
        
        // 藍色可跳轉連結字體 (支援 FB, IG, Threads, YouTube 等外部連結)
        val introTextRaw = if (introLink != "#" && introLink.isNotBlank()) introLink else "尚未提供介紹連結"
        val introHtml = if (introLink != "#" && introLink.isNotBlank()) {
            "<a href=\"$introLink\" target=\"_blank\" style=\"color: #1A73E8; text-decoration: underline;\">$introTextRaw</a>"
        } else {
            "<span style=\"color: #888888;\">$introTextRaw</span>"
        }

        val phoneHtml = if (card.contact.phone.isNotBlank()) {
            "<a href=\"tel:${card.contact.phone}\" style=\"color: #1A73E8; text-decoration: underline;\">${card.contact.phone}</a>"
        } else {
            "未提供"
        }

        val lineHtml = if (lineLink != "#") {
            "<a href=\"$lineLink\" target=\"_blank\" style=\"color: #1A73E8; text-decoration: underline;\">$contactDisplay</a>"
        } else {
            contactDisplay
        }

        val typeDesc = if (card.drawer_type == "1B") "我要供給" else "我要需求"

        return """
            <div class="vertical-card-item" data-index="${card.idx}">
              <!-- 卡片識別標頭 -->
              <div class="card-header">
                <span class="card-idx">${card.idx}</span>
                <h3 class="card-title">${card.t} (${card.dist}) - 區段: ${card.category_code} [$typeDesc]</h3>
                <span class="card-time">${card.time}</span>
              </div>

              <!-- 區塊 1：📍 位置與導航 -->
              <div class="card-section">
                <div class="section-label">📍 區塊 1：位置與導航</div>
                <p class="section-content">
                  發布時間: ${card.time} | 座標: (${card.lat}, ${card.lng}) | 距離: ${card.dist}<br>
                  <a href="$mapLink" target="_blank" style="color: #1A73E8; text-decoration: underline;">[Google Map 導航]</a>
                </p>
              </div>

              <!-- 區塊 2：📌 標題與說明內容 -->
              <div class="card-section">
                <div class="section-label">📌 區塊 2：標題與說明內容</div>
                <p class="section-content">
                  <b>${card.t}</b><br>
                  ${card.desc} (展開)
                </p>
              </div>

              <!-- 區塊 3：🔗 聯絡與外部跳轉 -->
              <div class="card-section">
                <div class="section-label">🔗 區塊 3：聯絡與外部跳轉</div>
                <p class="section-content">
                  電話: $phoneHtml<br>
                  聯絡對接: $lineHtml<br>
                  個人/產品介紹: $introHtml
                </p>
              </div>

              <!-- 區塊 4：⚙️ 本地動作與狀態管理 -->
              <div class="card-section">
                <div class="section-label">⚙️ 區塊 4：本地動作與狀態管理</div>
                <p class="section-content">
                  <a href="#delete" style="color: #1A73E8; text-decoration: underline;">[✕ 下架 / 全網刪除]</a> | 
                  <a href="#list" style="color: #1A73E8; text-decoration: underline;">[↑ 向上滑動查看需求列表]</a>
                </p>
              </div>

              <!-- 垂直縱向 4 功能按鈕清單 (Vertical Stack Action Buttons) -->
              <div class="card-actions-vertical">
                <a href="$lineLink" class="btn-action line-action" target="_blank">$lineHtml</a>
                <a href="$phoneLink" class="btn-action phone-action">$phoneDisplay</a>
                <a href="$mapLink" class="btn-action map-action" target="_blank">📍 地圖導航</a>
                <a href="$introLink" class="btn-action intro-action" target="_blank">$introDisplay</a>
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
    fun wrapHtmlDocument(
        cardsHtml: String,
        outlineHtml: String = "",
        cardCount: Int = 0,
        sectionCode: String = "2-1",
        drawerType: String = "1A"
    ): String {
        val sectionAnchor = sectionCode.replace("-", "_")
        val categoryTitle = CategoryRepository.getCategoryTitle(sectionCode, null)
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
                a, a:link, a:visited, a:hover, a:active {
                  color: #1A73E8;
                  text-decoration: underline;
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
                .section-content a {
                  color: #1A73E8;
                  text-decoration: underline;
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
                .smart-prompt-footer {
                  background: #181828;
                  border: 1.5px solid #00E5FF;
                  border-radius: 12px;
                  padding: 16px;
                  margin-top: 20px;
                  margin-bottom: 12px;
                  text-align: center;
                  box-shadow: 0 4px 12px rgba(0, 229, 255, 0.2);
                }
              </style>
            </head>
            <body>
              <div class="pack-header-title">📋 一鍵打包 垂直縱向檢視 (共 $cardCount 筆)</div>
              $outlineHtml
              <div class="divider-title">=== [目標卡片與一鍵打包草稿 (共 $cardCount 筆)] ===</div>
              <div class="vertical-card-list">
                $cardsHtml
              </div>
              <div class="smart-prompt-footer">
                <div style="color: #FFFFFF; font-size: 14px; font-weight: bold; margin-bottom: 10px; line-height: 1.6; text-align: left;">
                  當使用者點擊 👉 【是】 時，系統會針對 [$sectionCode] $categoryTitle 專屬模組載入對應的 JSON 卡片資訊與詳細選單（如：需求發布卡片、聯絡欄位、付款方式與送餐地點等欄位）。<br>
                  您需要針對特定的標頭（如 [$sectionCode] 或其他項）輸出對應的詳細 JSON 卡片資料結構嗎？
                </div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
                  <a href="#expand_section_${sectionAnchor}_$drawerType" class="btn-action" style="background: #00E5FF; color: #000000; text-align: center; padding: 10px 16px; border-radius: 8px; text-decoration: none; font-weight: bold; font-size: 15px; display: inline-block;">
                    【是】
                  </a>
                  <span style="color: #00FFC2; font-weight: bold; font-size: 16px;">是+√</span>
                </div>
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
        val firstSectionCode = cards.firstOrNull()?.category_code.orEmpty().ifBlank { "2-1" }
        val firstDrawerType = cards.firstOrNull()?.drawer_type.orEmpty().ifBlank { "1A" }
        return wrapHtmlDocument(cardsHtml, outlineHtml, cards.size, firstSectionCode, firstDrawerType)
    }

    /**
     * 一鍵打包並渲染 JSON 格式卡片資料之標準入口
     */
    fun packageAndRenderJson(cards: List<DraftCardModel>): String {
        return packageAndRenderJsonWithOutline(null, cards)
    }
}

