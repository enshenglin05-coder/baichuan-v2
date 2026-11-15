package com.example.myapplication01

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * 黑名單檢舉管控倉庫 (Blocklist & Report Audit Repository)
 *
 * 1. 目標資料夾結構規範：
 *    - 倉庫根目錄下配置專屬的檢舉與黑名單審核資料夾 (/audit_blocklist_repo/)。
 *    - 專門存放遭前端檢舉、格式或權限異常但需人工介入審查的隔離卡片 JSON 封包。
 *
 * 2. 自動化判定與初始化邏輯：
 *    - 若尚未建置：自動初始化該目錄路徑，確保後續被標記檢舉的卡片能夠正確被隔離並推送到此處供人工忽略或永久刪除。
 */
object AuditBlocklistManager {

    private var auditDirectory: File? = null
    private val gson: Gson get() = BaichuanApiService.gson

    /**
     * 初始化檢舉與黑名單審核倉庫目錄 (/audit_blocklist_repo/)
     */
    fun ensureAuditAndBlocklistRepositoryInitialized(context: Context? = null): File {
        auditDirectory?.let {
            if (it.exists()) return it
        }
        val baseDir = context?.applicationContext?.filesDir ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp", "audit_temp")
        val auditDir = File(baseDir, "audit_blocklist_repo")
        if (!auditDir.exists()) {
            auditDir.mkdirs()
            try {
                android.util.Log.d("AuditBlocklist", "📁 [自動化建置] 成功初始化黑名單檢舉管控倉庫目錄: ${auditDir.absolutePath}")
            } catch (_: Throwable) {
                println("[AuditBlocklist] 📁 成功初始化黑名單檢舉管控倉庫目錄: ${auditDir.absolutePath}")
            }
        }
        auditDirectory = auditDir
        return auditDir
    }

    /**
     * 推送遭檢舉或異常之卡片 JSON 封包至隔離審核倉庫
     */
    fun pushToAuditRepository(cardId: String, cardJson: String, context: Context? = null): File? {
        return try {
            val dir = ensureAuditAndBlocklistRepositoryInitialized(context)
            val file = File(dir, "$cardId.json")
            file.writeText(cardJson, Charsets.UTF_8)
            try {
                android.util.Log.d("AuditBlocklist", "🛡️ [隔離審核] 已成功將受檢舉/異常卡片推送至 /audit_blocklist_repo/: ${file.name}")
            } catch (_: Throwable) {
                println("[AuditBlocklist] 🛡️ 已推送到審核倉庫: ${file.name}")
            }
            // 同步推送至遠端 GitHub 倉庫的 audit_blocklist_repo/ 目錄
            try {
                BaichuanApiService.publishAuditBlocklistPacket(cardId, cardJson)
            } catch (_: Throwable) {}
            file
        } catch (e: Exception) {
            try {
                android.util.Log.e("AuditBlocklist", "❌ 推送至審核倉庫失敗: ${e.message}", e)
            } catch (_: Throwable) {}
            null
        }
    }

    /**
     * 讀取審核倉庫中所有被隔離列管的檢舉卡片清單
     */
    fun getAuditedCards(context: Context? = null): List<String> {
        val dir = ensureAuditAndBlocklistRepositoryInitialized(context)
        val files = dir.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) } ?: return emptyList()
        val list = mutableListOf<String>()
        for (file in files) {
            try {
                list.add(file.readText(Charsets.UTF_8))
            } catch (_: Exception) {}
        }
        return list
    }

    /**
     * 從審核倉庫中永久刪除或解除隔離
     */
    fun removeAuditCard(cardId: String, context: Context? = null): Boolean {
        return try {
            val dir = ensureAuditAndBlocklistRepositoryInitialized(context)
            val file = File(dir, "$cardId.json")
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
