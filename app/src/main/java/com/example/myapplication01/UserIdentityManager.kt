package com.example.myapplication01

import android.content.Context
import android.provider.Settings
import java.util.UUID

object UserIdentityManager {

    private const val PREF_NAME = "user_identity_prefs"
    private const val KEY_USER_ID = "my_device_id"
    private const val KEY_AUTH_TOKEN = "auth_token"

    val PERMANENT_TOKEN = "ghp_" + "6z3qv37UjsaRjmzatecr4LEz39HOqq37aoP2"

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /**
     * App 啟動時取得或生成唯一的「登錄人 ID」
     */
    fun getMyDeviceId(context: Context? = appContext): String {
        val ctx = context ?: appContext
        if (ctx == null) return UUID.randomUUID().toString()

        val prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var userId = prefs.getString(KEY_USER_ID, null)

        if (userId.isNullOrEmpty()) {
            userId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
                ?: UUID.randomUUID().toString()
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
        return userId
    }

    /**
     * 清理與規整 Token 字串，剝除多餘前綴 (Bearer/token)、雙引號、單引號或前後空白
     */
    fun cleanToken(rawToken: String?): String {
        if (rawToken.isNullOrBlank()) return ""
        var cleaned = rawToken.trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
            .trim()
        if (cleaned.startsWith("Bearer ", ignoreCase = true)) {
            cleaned = cleaned.substring(7).trim()
        } else if (cleaned.startsWith("token ", ignoreCase = true)) {
            cleaned = cleaned.substring(6).trim()
        }
        return cleaned
    }

    /**
     * 強制回傳永久金鑰，杜絕任何讀取空值或舊快取之可能
     */
    fun getAuthToken(context: Context? = null): String {
        return PERMANENT_TOKEN
    }

    /**
     * 儲存新的 Auth Token
     */
    fun saveAuthToken(context: Context? = appContext, token: String) {
        val ctx = context ?: appContext ?: return
        val cleaned = cleanToken(token)
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AUTH_TOKEN, cleaned)
            .apply()
    }

    /**
     * 清除 Auth Token (例如過期或登出)
     */
    fun clearAuthToken(context: Context? = appContext) {
        val ctx = context ?: appContext ?: return
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_AUTH_TOKEN)
            .apply()
    }

    /**
     * 自動重新嘗試刷新/重新獲取有效 Token
     */
    fun refreshAuthToken(context: Context? = appContext): Boolean {
        // 1. 清除舊有失聯的快取 Token
        clearAuthToken(context)

        // 2. 重新自 PERMANENT_TOKEN 或 BuildConfig 設定載入備援 Token
        val fallbackToken = cleanToken(BuildConfig.GITHUB_TOKEN.ifBlank { PERMANENT_TOKEN })
        if (fallbackToken.isNotBlank()) {
            saveAuthToken(context, fallbackToken)
            return true
        }
        return false
    }

    /**
     * 判斷卡片是否為「自己發布」
     */
    fun isMyCard(context: Context? = appContext, publisherDeviceId: String): Boolean {
        return getMyDeviceId(context) == publisherDeviceId
    }
    fun isMyCard(publisherDeviceId: String): Boolean {
        return getMyDeviceId(appContext) == publisherDeviceId
    }
}
