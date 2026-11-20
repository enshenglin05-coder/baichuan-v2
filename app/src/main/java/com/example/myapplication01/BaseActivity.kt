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

        /*
        // [已註解] App 冷啟動與生命週期初始化自動掛載寫入與同步（目前已不需要，避免背景自動產生或觸發同步寫入）
        try {
            ServerlessRepositoryInitializer.initializeRepositories(filesDir)
            AuditBlocklistManager.ensureAuditAndBlocklistRepositoryInitialized(this)
            TtsRepositoryManager.ensureTtsRepositoryInitialized(this)
        } catch (_: Throwable) {}
        */
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
    }

    override fun startActivity(intent: Intent?, options: Bundle?) {
        super.startActivity(intent, options)
    }
}
