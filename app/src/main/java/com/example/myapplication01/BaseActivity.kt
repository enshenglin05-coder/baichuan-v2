package com.example.myapplication01

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BaichuanApiService.init(this)
        GlobalTtsPlayer.init(this)
        DeityBlessingTTS.init(this)

        // 實體執行：初始化零伺服器遠端倉庫目錄與防線
        try {
            ServerlessRepositoryInitializer.initializeRepositories(filesDir)
            AuditBlocklistManager.ensureAuditAndBlocklistRepositoryInitialized(this)
            TtsRepositoryManager.ensureTtsRepositoryInitialized(this)
        } catch (_: Throwable) {}
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
    }
}
