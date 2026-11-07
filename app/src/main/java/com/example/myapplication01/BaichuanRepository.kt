package com.example.myapplication01

import android.content.Context
import com.example.myapplication01.repository.core.GeoBoundingBoxFilter
import java.util.concurrent.ConcurrentHashMap

/**
 * 檢舉處理結果列舉
 */
enum class ReportResult {
    REPORT_SUCCESS,           // 檢舉成功（未達3次）
    TRIGGER_HIDE_AND_UPLOAD,  // 滿3次：觸發隱藏並上報
    ALREADY_REPORTED,         // 同一裝置重複檢舉
    EXPIRED,                  // 卡片已超過 12 小時過期
    NOT_FOUND                 // 找不到卡片
}

/**
 * 百川 1.0 數據倉儲 (Repository Drawer)
 * 負責本地記憶體管理、12 小時 TTL 自動清理與 50km Bounding Box 過濾
 */
object BaichuanRepository {

    // 改為分片存儲 (Partitioned Storage)：依照 category_code 分組，徹底解決前端全量篩選效能問題
    private val partitionedCards = ConcurrentHashMap<String, ConcurrentHashMap<String, RequirementCard>>()
    
    private const val MAX_REPORT_THRESHOLD = 3

    fun init(context: Context) {
        // 初始化在地端 JSON 資料庫與倉儲
        LocalCardDatabase.init(context)
        purgeExpiredCards()
        seedTestDrawerData()
    }

    init {
        // 初始化載入測試卡片與搜尋引擎情境推理 88 個卡片項目
        seedTestDrawerData()
    }

    /**
     * 注入測試卡片至記憶體（不上報遠端 API）
     */
    fun injectTestCards(cards: List<RequirementCard>) {
        cards.forEach { card ->
            val categoryMap = partitionedCards.getOrPut(card.category_code) { ConcurrentHashMap() }
            categoryMap[card.card_id] = card
        }
    }

    /**
     * 清空記憶體中所有抽屜卡片（測試隔離使用）
     */
    fun clearAll() {
        partitionedCards.clear()
    }

    /**
     * 批次載入預設測試抽屜資料 (1-1, 4-1, 4-2 及搜尋引擎情境推理 88 卡片) 並載入在地 JSON 資料庫卡片
     */
    fun seedTestDrawerData() {
        clearAll()
        DrawerBatchTester.seedRepositoryWithTestCards()
        SearchEngineReasoningEngine.generateAndPublish88Cards()
        loadLocalDatabaseCards()
    }

    /**
     * 從在地端 JSON 資料庫載入手動發布卡片至記憶體倉儲
     */
    fun loadLocalDatabaseCards(context: Context? = null) {
        val localCards = LocalCardDatabase.getAllLocalCards(context)
        if (localCards.isNotEmpty()) {
            injectTestCards(localCards)
            try {
                android.util.Log.d("BaichuanRepo", "📥 已從在地端 JSON 資料庫載入 ${localCards.size} 張手動卡片至記憶體")
            } catch (e: Throwable) {
                println("[BaichuanRepo] 📥 已從在地端 JSON 資料庫載入 ${localCards.size} 張手動卡片至記憶體")
            }
        }
    }

    /**
     * 從 Git Raw JSON 網址動態下載與刷新卡片
     */
    fun refreshCardsFromGit(
        gitRawUrl: String,
        onSuccess: (List<RequirementCard>) -> Unit,
        onError: (String) -> Unit
    ) {
        BaichuanApiService.refreshCardsFromGit(gitRawUrl, onSuccess, onError)
    }

    /**
     * 新增或更新卡片 (分片邏輯)
     * 1. 序列化並寫入在地端 JSON 資料庫 (檔名: $cardId.json)
     * 2. 讀取在地 JSON 檔案並透過 PUT 請求真正上傳至遠端 GitHub 的 cards/ 資料夾中
     */
    fun addOrUpdateCard(
        card: RequirementCard,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // 1. 本地 JSON 序列化與在地資料庫實體寫入
        LocalCardDatabase.saveCard(card)

        // 防止重複發布與優先覆蓋機制：以最後一筆為優先，跨所有分片移除該發布者過去的所有舊卡片
        if (card.publisher_device_id != "SYSTEM_ADMIN" && !card.is_test_only && card.publisher_device_id.isNotBlank()) {
            for ((_, categoryMap) in partitionedCards) {
                val keysToRemove = mutableListOf<String>()
                for ((key, existingCard) in categoryMap) {
                    if (existingCard.publisher_device_id == card.publisher_device_id) {
                        keysToRemove.add(key)
                    }
                }
                keysToRemove.forEach { categoryMap.remove(it) }
            }
        }

        val catCode = card.category_code
        val categoryMap = partitionedCards.getOrPut(catCode) { ConcurrentHashMap() }

        // 全新寫入最新卡片 (以最後一筆為優先)
        categoryMap[card.card_id] = card
        
        // 2. 雲端上傳通道對接：從在地資料庫讀取 JSON 檔案並上傳至遠端 GitHub cards/ 資料夾
        if (!card.is_test_only) {
            BaichuanApiService.publishCard(card, {
                onSuccess?.invoke()
            }, { error: String ->
                onError?.invoke(error)
            })
        } else {
            onSuccess?.invoke()
        }
    }

    /**
     * 自動清除/抹除所有超過 12 小時的暫存卡片資料 (真實與虛擬卡片一律生效)
     */
    fun purgeExpiredCards(nowMillis: Long = System.currentTimeMillis()) {
        for (map in partitionedCards.values) {
            val expiredKeys = map.filter { (_, card) -> card.isExpired(nowMillis) }.keys
            expiredKeys.forEach { map.remove(it) }
        }
    }

    /**
     * 確保 12大項97格矩陣之固定預設值卡片在記憶體中就緒
     */
    fun ensurePresetCardsLoaded() {
        var hasPresetCards = false
        for (map in partitionedCards.values) {
            if (map.values.any { it.isPresetCard() }) {
                hasPresetCards = true
                break
            }
        }
        if (!hasPresetCards) {
            DrawerBatchTester.seedRepositoryWithTestCards()
            SearchEngineReasoningEngine.generateAndPublish88Cards()
        }
    }

    /**
     * 獲取過濾後的卡片列表 (兩步驟依序載入：手動寫入/雲端卡片優先置頂，固定預設卡片帶入下方顯示)
     */
    fun getFilteredCards(
        userLat: Double?,
        userLng: Double?,
        categoryCodes: List<String>, 
        targetType: String,
        currentDeviceId: String,
        limitDistanceKm: Double = 50.0,
        onFetchComplete: (() -> Unit)? = null
    ): List<RequirementCard> {
        
        // 1. 自動執行 12 小時暫存抹除作業
        purgeExpiredCards()
        onFetchComplete?.invoke()

        // 2. 從「抽屜」(Partitions) 中提取數據
        val sourcePool = mutableListOf<RequirementCard>()
        
        if (categoryCodes.isEmpty()) {
            partitionedCards.values.forEach { map -> sourcePool.addAll(map.values) }
        } else {
            categoryCodes.forEach { code ->
                partitionedCards[code]?.values?.let { sourcePool.addAll(it) }
            }
        }

        // 3. 執行算能與業務過濾 (TTL, 12大項97格矩陣驗證, A/B 雙向交叉, GPS 經緯度距離數學函數)
        val box = if (userLat != null && userLng != null) {
            GeoBoundingBoxFilter.calculateBoundingBox(userLat, userLng, limitDistanceKm)
        } else null
        
        val validCards = sourcePool.filter { card ->
            // TTL 檢查 (支援動態 ttl_hours)
            if (card.isExpired()) return@filter false
            
            // 檢舉閾值檢查
            val isBlocked = card.moderation_status == "AUTO_HIDDEN" || card.reporter_ids.size >= MAX_REPORT_THRESHOLD
            if (isBlocked && card.accepted_by_device_id != currentDeviceId) return@filter false
            
            // 排除自己檢舉過的
            if (card.reporter_ids.contains(currentDeviceId)) return@filter false
            
            // A/B 雙向交叉邏輯角色對齊 (1A: 需求 vs 1B: 供給: A送B, B送A)
            if (card.drawer_type != targetType) return@filter false

            // 12大項97格直向矩陣驗證 (0-0 及 1-1 至 12-8)
            if (!com.example.myapplication01.repository.core.MatchingCore.validateCategoryCode(card.category_code)) return@filter false
            
            // 經緯度數學函數 (Bounding Box 與距離檢查)
            if (box != null && userLat != null && userLng != null) {
                val inBox = card.gps_lat in box.minLat..box.maxLat && card.gps_lng in box.minLon..box.maxLon
                if (!inBox) return@filter false
                
                val distance = DistanceUtils.calculateDistance(userLat, userLng, card.gps_lat, card.gps_lng)
                distance <= limitDistanceKm
            } else {
                false // 無座標時不顯示
            }
        }.map { it.copy(isSelfOwner = it.publisher_device_id == currentDeviceId) }

        // 4. 載入優先權順序 (Priority Sequence) 與三順位依序去重補位：
        // 【第一順位 (絕對優先)】與【第二順位 (動態卡片)】：使用者真實發布/動態拉取之主體卡片 (!isPresetCard)
        val realUserCards = validCards.filter { !it.isPresetCard() }
        
        // 收集已由真實卡片佔據之抽屜集合 (category_code + "_" + drawer_type)
        val occupiedDrawers = realUserCards.map { "${it.category_code}_${it.drawer_type}" }.toSet()

        // 【第三順位 (補位用)】：系統預設值，僅在該抽屜無任何真實卡片時填補空缺，絕不排擠或重複覆蓋真實卡片
        val fallbackPresetCards = validCards.filter { card ->
            card.isPresetCard() && "${card.category_code}_${card.drawer_type}" !in occupiedDrawers
        }

        // 兩步驟依序排序：真實卡片在前 (使用者自己發布的優先置頂)，預設補位卡片在後
        return (realUserCards + fallbackPresetCards).sortedWith(
            compareBy<RequirementCard> { card ->
                if (!card.isPresetCard()) 0 else 1
            }.thenByDescending { card ->
                card.isSelfOwner
            }.thenByDescending { card ->
                card.created_at
            }
        )
    }

    /**
     * 執行檢舉邏輯 (跨分片搜尋)
     * 實現 12 小時 TTL 與 3 次重複檢舉判定
     */
    fun reportCard(cardId: String, reporterId: String, reason: String): ReportResult {
        // 搜尋所有分片以定位卡片
        var targetCard: RequirementCard? = null
        var foundCategory: String? = null
        
        for ((cat, map) in partitionedCards) {
            if (map.containsKey(cardId)) {
                targetCard = map[cardId]
                foundCategory = cat
                break
            }
        }
        
        val card = targetCard ?: return ReportResult.NOT_FOUND
        val catCode = foundCategory ?: return ReportResult.NOT_FOUND
        
        // 1. 檢查卡片是否已超過 12 小時
        if (card.isExpired()) {
            return ReportResult.EXPIRED
        }

        // 2. 同一裝置ID 12小時內重複點擊無效
        if (card.reporter_ids.contains(reporterId)) {
            return ReportResult.ALREADY_REPORTED
        }

        // 3. 更新檢舉狀態
        val updatedReporters = card.reporter_ids.toMutableSet().apply { add(reporterId) }
        val updatedReportsByReason = card.reports_by_reason.toMutableMap()
        val reasonSet = updatedReportsByReason[reason]?.toMutableSet() ?: mutableSetOf()
        reasonSet.add(reporterId)
        updatedReportsByReason[reason] = reasonSet
        
        // 4. 針對「同一個檢舉選項」在 12 小時內累積達 3 次則自動觸發隱藏
        val isAutoHidden = reasonSet.size >= MAX_REPORT_THRESHOLD
        
        val updatedCard = card.copy(
            reporter_ids = updatedReporters,
            reports_by_reason = updatedReportsByReason,
            moderation_status = if (isAutoHidden) "AUTO_HIDDEN" else card.moderation_status
        )
        
        partitionedCards[catCode]?.put(cardId, updatedCard)
        
        if (isAutoHidden) {
            CardJsonExceptionHandler.quarantineReportedCard(updatedCard, reason, reasonSet.size)
        }

        // --- 提交檢舉至遠端風控系統與後台人工審核佇列 ---
        BaichuanApiService.reportCard(cardId, reason, reporterId)
        
        return if (isAutoHidden) {
            ReportResult.TRIGGER_HIDE_AND_UPLOAD
        } else {
            ReportResult.REPORT_SUCCESS
        }
    }

    /**
     * 更新讚/踩數值
     */
    fun updateVoteCounts(cardId: String, up: Int, down: Int) {
        for (map in partitionedCards.values) {
            if (map.containsKey(cardId)) {
                val card = map[cardId]
                if (card != null) {
                    map[cardId] = card.copy(like_count = up, dislike_count = down)
                }
                break
            }
        }
    }

    /**
     * 移除卡片 (跨分片與在地端 JSON 資料庫移除)
     * 並執行遠端刪除同步，支援回調
     */
    fun removeCard(cardId: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        LocalCardDatabase.deleteCard(cardId)
        var targetCard: RequirementCard? = null
        for (map in partitionedCards.values) {
            if (map.containsKey(cardId)) {
                targetCard = map.remove(cardId)
                break
            }
        }
        
        targetCard?.let { card ->
            BaichuanApiService.deleteCard(card, onSuccess, onError)
        } ?: onSuccess()
    }
}
