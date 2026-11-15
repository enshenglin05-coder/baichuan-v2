package com.example.myapplication01

import android.util.Log
import java.io.File

/**
 * 實體執行：零伺服器遠端倉庫目錄與防線初始化
 * 目標：強制在倉庫中建立專屬資料夾，禁止沙盒模擬。
 */
object ServerlessRepositoryInitializer {

    private const val TAG = "ServerlessInit"

    // 定義遠端倉庫對應的實體資料夾名稱
    const val TTS_REPOSITORY_FOLDER = "tts_repository_folder"
    const val AUDIT_BLOCKLIST_REPO = "audit_blocklist_repo"

    /**
     * 執行初始化與目錄建立
     */
    fun initializeRepositories(rootProjectDir: File) {
        try {
            // 1. 建立 TTS 語音卡片與每兩分鐘計次/時間戳憑證回推區
            val ttsFolder = File(rootProjectDir, TTS_REPOSITORY_FOLDER)
            if (!ttsFolder.exists()) {
                val createdTts = ttsFolder.mkdirs()
                Log.d(TAG, "建立 TTS 倉庫資料夾 [$TTS_REPOSITORY_FOLDER]: $createdTts")
                // 建立 .gitkeep 確保空資料夾能順利推送到 GitHub 遠端倉庫
                try {
                    File(ttsFolder, ".gitkeep").createNewFile()
                } catch (_: Throwable) {}
            }

            // 2. 建立黑名單檢舉與人工審核隔離區
            val auditFolder = File(rootProjectDir, AUDIT_BLOCKLIST_REPO)
            if (!auditFolder.exists()) {
                val createdAudit = auditFolder.mkdirs()
                Log.d(TAG, "建立黑名單審核隔離區 [$AUDIT_BLOCKLIST_REPO]: $createdAudit")
                try {
                    File(auditFolder, ".gitkeep").createNewFile()
                } catch (_: Throwable) {}
            }

            Log.d(TAG, "所有零伺服器倉庫實體防線已全面建置完畢。")
        } catch (e: Exception) {
            Log.e(TAG, "初始化倉庫資料夾失敗: ${e.message}", e)
        }
    }
}
