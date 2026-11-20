package com.example.myapplication01

import android.content.Context
import android.util.Log

/**
 * 語音廣告播放管理器 (TTS Ad Manager)
 * 負責管理 TTS 冠名廣告的篩選、投放、履約足跡記錄與歸零自動回收
 */
object TtsAdManager {

    private val adList = mutableListOf<TTSAdModel>()
    private val adLogsMap = mutableMapOf<String, MutableList<RecycledAdLog>>()
    private val recycledRecords = mutableListOf<RecycledAdRecord>()
    private var appContext: Context? = null

    private fun safeLogD(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    init {
        if (adList.isEmpty()) {
            val sampleAd = TTSAdModel(
                id = "AD_HURLUNGCHUANG_01",
                text = "中和廣濟宮開漳聖王聖王公守護您",
                location = TTSAdLocation(
                    latitude = HurlungchuangTestManager.TARGET_LATITUDE,
                    longitude = HurlungchuangTestManager.TARGET_LONGITUDE
                ),
                totalCount = 100,
                remainingCount = 100,
                status = TTSAdStatus.ACTIVE
            )
            adList.add(sampleAd)
        }
    }

    fun init(context: Context) {
        appContext = context.applicationContext
        LocalTtsAdDatabase.init(appContext!!)

        val loadedRecycled = LocalTtsAdDatabase.loadRecycledRecords(appContext)
        if (loadedRecycled.isNotEmpty()) {
            recycledRecords.clear()
            recycledRecords.addAll(loadedRecycled)
        }

        val loadedAds = LocalTtsAdDatabase.loadAds(appContext)
        val activeAds = loadedAds.filter { it.remainingCount > 0 && it.status == TTSAdStatus.ACTIVE }

        adList.clear()
        if (activeAds.isNotEmpty()) {
            adList.addAll(activeAds)
            safeLogD("TtsAdManager", "✅ [TTS 語音播報資料檢測] 成功接通並載入 ${activeAds.size} 筆有效語音播報個體 (資料存在，可順利接通播報)")
        } else {
            val localCards = LocalCardDatabase.getAllLocalCards(appContext)
            val playableCards = localCards.filter { card ->
                val archived = LocalCardDatabase.getArchivedCards(appContext).any { it.card_id == card.card_id }
                !archived
            }
            if (playableCards.isNotEmpty()) {
                val converted = playableCards.map { card ->
                    TTSAdModel(
                        id = card.card_id,
                        text = card.description,
                        location = TTSAdLocation(latitude = card.gps_lat, longitude = card.gps_lng),
                        totalCount = 3,
                        remainingCount = 3,
                        status = TTSAdStatus.ACTIVE
                    )
                }
                adList.addAll(converted)
                LocalTtsAdDatabase.saveAds(adList, appContext!!)
                safeLogD("TtsAdManager", "✅ [TTS 語音播報資料檢測] 從 LocalCardDatabase 成功轉入 ${converted.size} 筆有效播報個體 (資料存在，可順利接通播報)")
            } else {
                safeLogD("TtsAdManager", "ℹ️ [TTS 語音播報資料檢測] 無有效本機或後端卡片資料，維持靜默狀態，無預設預載入。")
            }
        }
    }

    /**
     * 註冊或更新廣告列表
     */
    fun updateAdList(ads: List<TTSAdModel>, context: Context? = null) {
        val ctx = context ?: appContext
        adList.clear()
        val activeAdsToKeep = mutableListOf<TTSAdModel>()
        for (ad in ads) {
            if (ad.remainingCount <= 0 || ad.status != TTSAdStatus.ACTIVE) {
                if (ctx != null) {
                    LocalCardDatabase.deleteCard(ad.id, ctx)
                    LocalCardDatabase.deleteArchivedCard(ad.id, ctx)
                    LocalTtsAdDatabase.deleteRecycledRecord(ad.id, ctx)
                    TtsRepositoryManager.deleteTtsPacket(ad.id, ctx)
                    BaichuanRepository.removeCard(ad.id)
                }
                safeLogD("TtsAdManager", "📦 [設定引擎歸零檢測] 發現 TTS 檔案 [${ad.id}] 剩餘次數歸零，直接拋棄，不做儲存動作。")
            } else {
                activeAdsToKeep.add(ad)
            }
        }
        adList.addAll(activeAdsToKeep)
        if (ctx != null) {
            LocalTtsAdDatabase.saveAds(adList, ctx)
        }
    }

    fun updateAdList(ads: List<TTSAdModel>) {
        updateAdList(ads, appContext)
    }

    /**
     * 從雲端/後端拉取卡片時，自動將新卡片註冊為 3 次播放 (設為 ACTIVE, remainingCount = 3)
     */
    fun syncCloudCardsToAdList(cards: List<RequirementCard>, context: Context? = null) {
        val ctx = context ?: appContext
        var changed = false
        for (card in cards) {
            if (!card.isValid() || card.isExpired() || BlocklistManager.isPullBlocked(card.publisher_device_id, card.card_id)) {
                continue
            }
            val existing = adList.find { it.id == card.card_id }
            if (existing == null) {
                val isDeletedOrArchived = if (ctx != null) {
                    LocalCardDatabase.getCard(card.card_id) == null && LocalCardDatabase.getArchivedCards(ctx).any { it.card_id == card.card_id }
                } else false

                if (!isDeletedOrArchived) {
                    if (ctx != null) {
                        LocalCardDatabase.saveCard(card, ctx)
                    }
                    val newAd = TTSAdModel(
                        id = card.card_id,
                        text = card.description,
                        location = TTSAdLocation(latitude = card.gps_lat, longitude = card.gps_lng),
                        totalCount = 3,
                        remainingCount = 3,
                        status = TTSAdStatus.ACTIVE
                    )
                    adList.add(newAd)
                    changed = true
                    safeLogD("TtsAdManager", "📥 [後端卡片同步載入] 卡片 [${card.card_id}] 已註冊為 3 次發音播報 (剩餘次數: 3)")
                }
            }
        }
        if (changed && ctx != null) {
            LocalTtsAdDatabase.saveAds(adList, ctx)
        }
    }

    /**
     * 取得目前所有的廣告列表
     */
    fun getAdList(): List<TTSAdModel> = adList.toList()

    /**
     * 取得已回收歸零封存的紀錄列表
     */
    fun getRecycledRecords(): List<RecycledAdRecord> = recycledRecords.toList()

    /**
     * 取得廣告履約播放足跡日誌
     */
    fun getAdPlayLogs(): Map<String, List<RecycledAdLog>> = adLogsMap.mapValues { it.value.toList() }

    /**
     * 尋找符合地點與狀態的可播放廣告
     */
    fun findPlayableAd(userLat: Double?, userLng: Double?, radiusKm: Double = 6.0): TTSAdModel? {
        val iterator = adList.iterator()
        val ctx = appContext
        while (iterator.hasNext()) {
            val ad = iterator.next()
            if (ad.remainingCount <= 0 || ad.status != TTSAdStatus.ACTIVE) {
                iterator.remove()
                if (ctx != null) {
                    LocalCardDatabase.deleteCard(ad.id, ctx)
                    LocalCardDatabase.deleteArchivedCard(ad.id, ctx)
                    LocalTtsAdDatabase.deleteRecycledRecord(ad.id, ctx)
                    TtsRepositoryManager.deleteTtsPacket(ad.id, ctx)
                    BaichuanRepository.removeCard(ad.id)
                }
                safeLogD("TtsAdManager", "📦 [尋找可播放廣告檢測] 發現 TTS 檔案 [${ad.id}] 倒數計時歸零，直接拋棄，不做儲存動作。")
            }
        }

        val activeAds = adList.filter { it.isPlayable }
        if (activeAds.isEmpty()) return null

        if ((userLat != null) && (userLng != null) && (userLat != 0.0) && (userLng != 0.0)) {
            val nearbyAd = activeAds.firstOrNull { it.isWithinRadius(userLat, userLng, radiusKm) }
            if (nearbyAd != null) {
                return nearbyAd
            }
        }

        return activeAds.firstOrNull()
    }

    /**
     * 結合冠名廣告發音文字
     */
    fun buildAdPrefixedText(mainText: String, ad: TTSAdModel?): String {
        return if (ad != null && ad.isPlayable && ad.text.isNotBlank() && ad.remainingCount > 0) {
            "本發音由${ad.text}贊助播出。$mainText"
        } else {
            // 計時器歸零或無數字時，回倉庫播放為空值（僅返回主文本，絕不固定再播放贊助廣告）
            mainText
        }
    }

    /**
     * 語音自然播放完成後扣減剩餘次數，若歸零則觸發實體檔案自動搬運 (歸位封存)
     */
    fun onAdPlayed(adId: String, context: Context? = null): Pair<TTSAdModel, RecycledAdRecord?>? {
        val index = adList.indexOfFirst { it.id == adId }
        if (index != -1) {
            val currentAd = adList[index]
            val updatedAd = currentAd.consumePlayCount()
            val ctx = context ?: appContext

            // 記錄履約足跡
            val logs = adLogsMap.getOrPut(adId) { mutableListOf() }
            logs.add(RecycledAdLog(timestamp = TTSAdModel.getCurrentIsoTimestamp(), triggerCount = 1))

            var archiveRecord: RecycledAdRecord? = null

            if (updatedAd.remainingCount <= 0 || updatedAd.status == TTSAdStatus.ARCHIVED_ZERO) {
                // 強制中斷續撥：計數歸零時絕對禁止觸發續撥，清空並停止整個播放佇列
                TtsPacketChainManager.stopChain()

                // 歸零直接拋棄（不做儲存動作）：
                // 1. 從主佇列 (adList) 移除
                adList.removeAt(index)
                LocalTtsAdDatabase.saveAds(adList, ctx)

                // 2. 清理本地端實體檔案與資料庫，不做儲存動作，避免幹擾
                LocalCardDatabase.deleteCard(adId, ctx)
                LocalCardDatabase.deleteArchivedCard(adId, ctx)
                LocalTtsAdDatabase.deleteRecycledRecord(adId, ctx)
                TtsRepositoryManager.deleteTtsPacket(adId, ctx)

                // 3. 從記憶體倉儲移除
                BaichuanRepository.removeCard(adId)

                // 4. 自動觸發資料同步模組：發送真實 PUT 請求將歸零狀態寫回遠端 GitHub 倉庫 (cards/{cardId}.json)
                BaichuanApiService.syncZeroedCardToGithub(
                    cardId = adId,
                    text = updatedAd.text,
                    lat = updatedAd.location.latitude,
                    lng = updatedAd.location.longitude,
                    totalCount = updatedAd.totalCount
                )

                // 5. 觸發專屬 TTS 倉庫背景非同步處理 (刪除活躍檔並推回後端取消)
                TtsRepositoryManager.zeroOutAndArchiveAsync(adId, ctx)

                safeLogD("TtsAdManager", "📦 [單筆播畢歸零/直接拋棄/不做儲存] 廣告 [$adId] 播放次數已歸零！直接從本地拋棄，推回後端倉庫取消。")
            } else {
                adList[index] = updatedAd
                LocalTtsAdDatabase.saveAds(adList, ctx)
                safeLogD("TtsAdManager", "🎯 廣告 [$adId] 觸發扣減完畢，剩餘次數: ${updatedAd.remainingCount}, 狀態: ${updatedAd.status}")
            }

            return Pair(updatedAd, archiveRecord)
        }
        return null
    }

    fun onAdPlayed(adId: String): Pair<TTSAdModel, RecycledAdRecord?>? {
        return onAdPlayed(adId, appContext)
    }
}
