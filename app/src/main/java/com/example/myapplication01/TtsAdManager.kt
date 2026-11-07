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
                text = "新北市中和區廣濟宮聖王公守護你",
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
        val loadedAds = LocalTtsAdDatabase.loadAds(appContext)
        if (loadedAds.isNotEmpty()) {
            adList.clear()
            adList.addAll(loadedAds)
        } else {
            LocalTtsAdDatabase.saveAds(adList, appContext!!)
        }

        val loadedRecycled = LocalTtsAdDatabase.loadRecycledRecords(appContext)
        if (loadedRecycled.isNotEmpty()) {
            recycledRecords.clear()
            recycledRecords.addAll(loadedRecycled)
        }
    }

    /**
     * 註冊或更新廣告列表
     */
    fun updateAdList(ads: List<TTSAdModel>, context: Context? = null) {
        adList.clear()
        adList.addAll(ads)
        val ctx = context ?: appContext
        LocalTtsAdDatabase.saveAds(adList, ctx)
    }

    fun updateAdList(ads: List<TTSAdModel>) {
        updateAdList(ads, appContext)
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
    fun findPlayableAd(userLat: Double?, userLng: Double?, radiusKm: Double = 10.0): TTSAdModel? {
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
        return if (ad != null && ad.isPlayable && ad.text.isNotBlank()) {
            "本發音由${ad.text}贊助播出。$mainText"
        } else {
            mainText
        }
    }

    /**
     * 播報與「倒數第二個字」觸發完成後扣減剩餘次數 (播一次、拉一次、扣一次)，若歸零則觸發實體檔案自動搬運 (屍體歸位)
     */
    fun onAdPlayed(adId: String, context: Context? = null): Pair<TTSAdModel, RecycledAdRecord?>? {
        val index = adList.indexOfFirst { it.id == adId }
        if (index != -1) {
            val currentAd = adList[index]
            val updatedAd = currentAd.consumePlayCount()
            val remainingCount = updatedAd.remainingCount
            val ctx = context ?: appContext

            // 記錄履約足跡
            val logs = adLogsMap.getOrPut(adId) { mutableListOf() }
            logs.add(RecycledAdLog(timestamp = TTSAdModel.getCurrentIsoTimestamp(), triggerCount = 1))

            var archiveRecord: RecycledAdRecord? = null

            if (updatedAd.remainingCount <= 0 || updatedAd.status == TTSAdStatus.ARCHIVED_ZERO) {
                // 歸零自動搬運（屍體歸位）：
                // 1. 從主佇列 (adList) 移除
                adList.removeAt(index)
                LocalTtsAdDatabase.saveAds(adList, ctx)

                // 2. 生成回收紀錄並搬移推送到專屬回收倉庫 (archived_warehouse & archived_cards)
                archiveRecord = RecycledAdRecord.fromAdModel(updatedAd, logs.toList())
                recycledRecords.add(archiveRecord)

                LocalTtsAdDatabase.archiveZeroAd(updatedAd, archiveRecord, ctx)
                LocalCardDatabase.archiveCardFile(adId, ctx)

                // 3. 從記憶體倉儲移除
                BaichuanRepository.removeCard(adId)

                safeLogD("CARD_COUNT", "🔥 倒數第二字觸發成功！當前剩餘次數: " + remainingCount)
                safeLogD("TtsAdManager", "📦 [歸零自動搬運/屍體歸位] 廣告 [$adId] 播放次數已歸零！已從主佇列中抹除，並成功將實體檔案搬移至專屬回收倉庫 (ArchiveID: ${archiveRecord.archiveId})")
            } else {
                adList[index] = updatedAd
                LocalTtsAdDatabase.saveAds(adList, ctx)
                safeLogD("CARD_COUNT", "🔥 倒數第二字觸發成功！當前剩餘次數: " + remainingCount)
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
