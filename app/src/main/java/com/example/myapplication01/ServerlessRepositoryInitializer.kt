package com.example.myapplication01

import java.io.File

/**
 * 零伺服器遠端倉庫目錄管理（已嚴格停用所有自動冷啟動掛載、預設保持檔案與測試封包生成）
 * 鐵律：當路徑或資料夾為空值時，絕對不要自動建立、不要補入任何預設資料（如 .gitkeep）、不要做任何測試。
 */
object ServerlessRepositoryInitializer {

    const val TTS_REPOSITORY_FOLDER = "tts_repository_folder"
    const val AUDIT_BLOCKLIST_REPO = "audit_blocklist_repo"

    /**
     * 執行初始化與目錄建立（已停用自動建立與預設檔案寫入）
     */
    fun initializeRepositories(rootProjectDir: File) {
        // [嚴格鐵律] 當路徑或資料夾為空值時，絕對不要自動建立、不要補入任何預設資料（如 .gitkeep）、不要做任何測試。
    }
}
