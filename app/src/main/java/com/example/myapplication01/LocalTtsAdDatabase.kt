package com.example.myapplication01

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * 本地端 TTS 語音自定義播放器資料庫與封存倉庫管理員 (Local TTS Ad Database & Archived Warehouse)
 * 負責持久化儲存:
 * 1. 前半部: 經緯度座標與套用距離範圍 (TTSAdLocation)
 * 2. 中間: 觸發點文字內容 (鎖定倒數第二個字觸發機制)
 * 3. 後半部: 倒數計時器 (記錄剩餘播放次數，從 10 次開始倒數)
 * 4. 封存區/歷史倉庫: 歸零自動歸位之已播放完畢資料 (RecycledAdRecord)
 */
object LocalTtsAdDatabase {

    private var dbDirectory: File? = null
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private const val TTS_ADS_FILE = "local_tts_ads.json"
    private const val RECYCLED_RECORDS_FILE = "local_recycled_records.json"

    private fun safeLogD(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (_: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun safeLogE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) {
                android.util.Log.e(tag, msg, tr)
            } else {
                android.util.Log.e(tag, msg)
            }
        } catch (_: Throwable) {
            println("[$tag] $msg ${tr?.message ?: ""}")
        }
    }

    fun init(context: Context) {
        val baseDir = context.applicationContext.filesDir
        val ttsDir = File(baseDir, "tts_ad_db")
        if (!ttsDir.exists()) {
            ttsDir.mkdirs()
        }
        dbDirectory = ttsDir
        safeLogD("LocalTtsAdDB", "📁 本地端 TTS 播放器資料庫與封存倉庫初始化完成: ${ttsDir.absolutePath}")
    }

    private fun getDirectory(context: Context? = null): File {
        dbDirectory?.let { return it }
        val ctx = context?.applicationContext
        val baseDir = ctx?.filesDir ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp", "tts_ad_temp")
        val ttsDir = File(baseDir, "tts_ad_db")
        if (!ttsDir.exists()) {
            ttsDir.mkdirs()
        }
        dbDirectory = ttsDir
        return ttsDir
    }

    /**
     * 儲存 TTS 廣告播放資料庫
     */
    fun saveAds(ads: List<TTSAdModel>, context: Context? = null) {
        try {
            val dir = getDirectory(context)
            val file = File(dir, TTS_ADS_FILE)
            val json = gson.toJson(ads)
            file.writeText(json, Charsets.UTF_8)
            safeLogD("LocalTtsAdDB", "💾 成功儲存 ${ads.size} 筆 TTS 播放器廣告至本地資料庫")
        } catch (e: Exception) {
            safeLogE("LocalTtsAdDB", "❌ 儲存 TTS 廣告資料庫失敗: ${e.message}")
        }
    }

    /**
     * 載入 TTS 廣告播放資料庫
     */
    fun loadAds(context: Context? = null): List<TTSAdModel> {
        try {
            val dir = getDirectory(context)
            val file = File(dir, TTS_ADS_FILE)
            if (!file.exists()) return emptyList()
            val json = file.readText(Charsets.UTF_8)
            val type = object : TypeToken<List<TTSAdModel>>() {}.type
            return gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            safeLogE("LocalTtsAdDB", "❌ 載入 TTS 廣告資料庫失敗: ${e.message}")
            return emptyList()
        }
    }

    /**
     * 儲存已歸零之回收倉庫/歷史倉庫資料與專屬屍體歸位檔案
     */
    fun saveRecycledRecords(records: List<RecycledAdRecord>, context: Context? = null) {
        try {
            val dir = getDirectory(context)
            val file = File(dir, RECYCLED_RECORDS_FILE)
            val json = gson.toJson(records)
            file.writeText(json, Charsets.UTF_8)
            safeLogD("LocalTtsAdDB", "💾 成功儲存 ${records.size} 筆已歸零封存紀錄至歷史倉庫")
        } catch (e: Exception) {
            safeLogE("LocalTtsAdDB", "❌ 儲存回收倉庫紀錄失敗: ${e.message}")
        }
    }

    /**
     * 歸零自動搬運與屍體歸位：
     * 將已歸零廣告寫入專屬已封存/回收倉庫 (`archived_warehouse/`)
     */
    fun archiveZeroAd(ad: TTSAdModel, record: RecycledAdRecord, context: Context? = null) {
        try {
            val dir = getDirectory(context)
            val archiveWarehouse = File(dir, "archived_warehouse")
            if (!archiveWarehouse.exists()) {
                archiveWarehouse.mkdirs()
            }
            val adFile = File(archiveWarehouse, "${ad.id}.json")
            val json = gson.toJson(ad)
            adFile.writeText(json, Charsets.UTF_8)

            // 同步更新回收紀錄清單
            val currentRecords = loadRecycledRecords(context).toMutableList()
            if (currentRecords.none { it.archiveId == record.archiveId }) {
                currentRecords.add(record)
                saveRecycledRecords(currentRecords, context)
            }

            safeLogD(
                "LocalTtsAdDB",
                "📦 [屍體歸位] 廣告 [${ad.id}] 次數歸零！實體檔案已成功推送至專屬回收倉庫: ${adFile.absolutePath}"
            )
        } catch (e: Exception) {
            safeLogE("LocalTtsAdDB", "❌ 歸位寫入專屬回收倉庫失敗: ${e.message}")
        }
    }

    /**
     * 載入已歸零之回收倉庫/歷史倉庫資料
     */
    fun loadRecycledRecords(context: Context? = null): List<RecycledAdRecord> {
        try {
            val dir = getDirectory(context)
            val file = File(dir, RECYCLED_RECORDS_FILE)
            if (!file.exists()) return emptyList()
            val json = file.readText(Charsets.UTF_8)
            val type = object : TypeToken<List<RecycledAdRecord>>() {}.type
            return gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            safeLogE("LocalTtsAdDB", "❌ 載入回收倉庫紀錄失敗: ${e.message}")
            return emptyList()
        }
    }

    /**
     * 【存棒倉庫控制 - 刪除按鍵】：徹底從回收倉庫刪除歸零紀錄與檔案
     */
    fun deleteRecycledRecord(adId: String, context: Context? = null): Boolean {
        return try {
            val dir = getDirectory(context)
            val records = loadRecycledRecords(context).filterNot { it.adId == adId || it.archiveId == adId }
            saveRecycledRecords(records, context)

            val archiveWarehouse = File(dir, "archived_warehouse")
            val adFile = File(archiveWarehouse, "$adId.json")
            if (adFile.exists()) {
                adFile.delete()
            }
            safeLogD("LocalTtsAdDB", "🗑️ [存棒銷毀] 已徹底刪除廣告回收紀錄與檔案 [$adId]")
            true
        } catch (e: Exception) {
            safeLogE("LocalTtsAdDB", "❌ 刪除廣告回收紀錄失敗 [$adId]: ${e.message}")
            false
        }
    }
}
