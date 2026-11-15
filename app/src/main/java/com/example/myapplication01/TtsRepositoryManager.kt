package com.example.myapplication01

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TTS 語音卡片倉庫目錄管理器 (TTS Repository Manager)
 *
 * 1. 目標資料夾結構與獨立原則：
 *    - 於專案根目錄/應用檔案目錄中確保存在專屬的 TTS 資料夾 (/tts_repository_folder/)。
 *    - 職責：專門用來存放每兩分鐘自動推播、帶有時間戳與倒數計次（如 100 剩餘次數）的 TTS 封包 JSON 檔案。
 *    - 與前端一般卡片渲染、黑名單檢舉審核資料夾（/audit_blocklist_repo/）完全獨立，避免模組互相干擾。
 *
 * 2. 自動化判定與初始化邏輯：
 *    - 若 IDE 檢測到該路徑已存在則直接沿用；若不存在則自動初始化建置，確保後端/管理端能夠隨時點開該資料夾與 JSON 檔案進行憑證稽核。
 */
object TtsRepositoryManager {

    private var ttsRepositoryDirectory: File? = null
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    data class TtsPacketRecord(
        val packetId: String,
        val text: String,
        val remainingCount: Int = 100,
        val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
    )

    /**
     * 自動化檢測與初始化 TTS 語音卡片倉庫目錄 (/tts_repository_folder/)
     */
    fun ensureTtsRepositoryInitialized(context: Context? = null): File {
        ttsRepositoryDirectory?.let {
            if (it.exists()) return it
        }
        val baseDir = context?.applicationContext?.filesDir ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp", "tts_repo_temp")
        val ttsRepoDir = File(baseDir, "tts_repository_folder")
        if (!ttsRepoDir.exists()) {
            ttsRepoDir.mkdirs()
            try {
                android.util.Log.d("TtsRepository", "📁 [自動化建置] 成功初始化專屬 TTS 語音卡片倉庫目錄: ${ttsRepoDir.absolutePath}")
            } catch (_: Throwable) {
                println("[TtsRepository] 📁 成功初始化專屬 TTS 語音卡片倉庫目錄: ${ttsRepoDir.absolutePath}")
            }
        }
        ttsRepositoryDirectory = ttsRepoDir
        return ttsRepoDir
    }

    /**
     * 儲存或更新 2 分鐘自動推播、帶時間戳與倒數計次的 TTS 封包 JSON 檔案至 /tts_repository_folder/
     */
    fun saveTtsPacket(packetId: String, text: String, remainingCount: Int = 100, context: Context? = null): File? {
        return try {
            val dir = ensureTtsRepositoryInitialized(context)
            val record = TtsPacketRecord(
                packetId = packetId,
                text = text,
                remainingCount = remainingCount
            )
            val file = File(dir, "$packetId.json")
            val json = gson.toJson(record)
            file.writeText(json, Charsets.UTF_8)
            try {
                android.util.Log.d("TtsRepository", "📢 [TTS 倉庫推播] 成功寫入帶有時間戳與剩餘次數 ($remainingCount) 的 TTS 封包: ${file.name}")
            } catch (_: Throwable) {
                println("[TtsRepository] 📢 成功寫入 TTS 封包: ${file.name}")
            }
            // 同步推送至遠端 GitHub 倉庫的 tts_repository_folder/ 目錄 (DTS JSON 封包格式)
            try {
                BaichuanApiService.publishTtsPacketToHub(packetId, json)
            } catch (_: Throwable) {}
            file
        } catch (e: Exception) {
            try {
                android.util.Log.e("TtsRepository", "❌ 寫入 TTS 封包失敗: ${e.message}", e)
            } catch (_: Throwable) {}
            null
        }
    }

    /**
     * 載入 /tts_repository_folder/ 中所有 TTS 封包記錄
     */
    fun loadAllTtsPackets(context: Context? = null): List<TtsPacketRecord> {
        val dir = ensureTtsRepositoryInitialized(context)
        val files = dir.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) } ?: return emptyList()
        val list = mutableListOf<TtsPacketRecord>()
        for (file in files) {
            try {
                val json = file.readText(Charsets.UTF_8)
                val record = gson.fromJson(json, TtsPacketRecord::class.java)
                if (record != null) {
                    list.add(record)
                }
            } catch (_: Exception) {}
        }
        return list.sortedByDescending { it.timestamp }
    }

    /**
     * 刪除指定的 TTS 封包檔案
     */
    fun deleteTtsPacket(packetId: String, context: Context? = null): Boolean {
        return try {
            val dir = ensureTtsRepositoryInitialized(context)
            val file = File(dir, "$packetId.json")
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
