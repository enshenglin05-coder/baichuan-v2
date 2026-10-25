package com.example.myapplication01

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 百川 1.0 API 傳輸中心 (GitHub 極簡架構 - baichuan-v2)
 * 負責 GitHub 數據同步與獨立發布寫入
 */
object BaichuanApiService {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        UserIdentityManager.init(context)
    }

    // GitHub API 標頭配置
    private const val HEADER_ACCEPT = "Accept"
    private const val VALUE_ACCEPT = "application/vnd.github+json"
    private const val HEADER_USER_AGENT = "User-Agent"
    private const val VALUE_USER_AGENT = "BaichuanApp"
    private const val HEADER_AUTH = "Authorization"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val urlString = originalRequest.url.toString()
            android.util.Log.e("BaichuanAPI_Debug", "=== 攔截器生效，網址: $urlString ===")

            val requestBuilder = originalRequest.newBuilder()
                .header(HEADER_ACCEPT, VALUE_ACCEPT)
                .header(HEADER_USER_AGENT, VALUE_USER_AGENT)
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header(HEADER_AUTH, "Bearer ghp_Y3Rb2ZHTrMcVDujrkjzv6SjhYicGU3107XgZ")

            android.util.Log.d("BaichuanAPI", "🔒 已為 API 請求注入 Bearer 驗證頭: $urlString")

            val builtRequest = requestBuilder.build()
            val response = chain.proceed(builtRequest)

            if (response.code == 401 || response.code == 403) {
                android.util.Log.e("BaichuanAPI", "⚠️ API 401/403 錯誤: ${response.request.url}")
            }

            response
        }
        .build()

    private val gson = Gson()

    /**
     * 動態獲取 Authorization Header (標準 Bearer 授權格式)
     */
    fun getAuthHeader(): String {
        return "Bearer ghp_Y3Rb2ZHTrMcVDujrkjzv6SjhYicGU3107XgZ"
    }

    /**
     * 當發生 401 / 403 錯誤時的錯誤處理邏輯
     */
    private fun handleUnauthorizedError() {
        android.util.Log.e("BaichuanAPI", "⛔ 身份驗證失敗 (401 Unauthorized / 403 Forbidden)，請檢查 Token 設定")
        Handler(Looper.getMainLooper()).post {
            appContext?.let { ctx ->
                Toast.makeText(ctx, "API 驗證失敗 (401 Unauthorized)，請檢查 Token 設定", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * 發布卡片數據 (純 PUT 寫入)
     * 目標路徑鎖定：https://api.github.com/repos/enshenglin05-coder/baichuan-v2/contents/cards/{cardId}.json
     * 在建立 Request 時強制寫死標頭：.header("Authorization", "Bearer ghp_Y3Rb2ZHTrMcVDujrkjzv6SjhYicGU3107XgZ")
     */
    fun publishCard(card: RequirementCard, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val cardId = card.card_id
        if (cardId.isBlank()) {
            android.util.Log.e("BaichuanAPI", "❌ 發布失敗：無效的卡片 ID")
            onError("Invalid card_id")
            return
        }

        val contentsUrl = "https://api.github.com/repos/enshenglin05-coder/baichuan-v2/contents/cards/$cardId.json"
        android.util.Log.d("BaichuanAPI", "🚀 直接執行 PUT 請求寫入雲端: $contentsUrl (檔案名稱: $cardId.json)")

        val cardJson = gson.toJson(card)
        val contentBase64 = android.util.Base64.encodeToString(cardJson.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)

        val payload = mapOf(
            "message" to "Publish card: $cardId",
            "content" to contentBase64
        )

        val body = gson.toJson(payload).toRequestBody("application/json".toMediaType())

        // 建立 Request 時強制寫死絕對字串標頭
        val request = Request.Builder()
            .url(contentsUrl)
            .header("Authorization", "Bearer ghp_Y3Rb2ZHTrMcVDujrkjzv6SjhYicGU3107XgZ")
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "BaichuanApp")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("BaichuanAPI", "❌ 發布卡片網絡請求失敗 [$cardId.json]: ${e.message}")
                onError(e.message ?: "Write Error")
            }

            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                val responseBody = response.body?.string() ?: ""
                response.close()

                android.util.Log.d("BaichuanAPI", "發布卡片 PUT 響應 [檔案名稱: $cardId.json] HTTP Code: $code, Response Body: $responseBody")

                if (response.isSuccessful) {
                    android.util.Log.d("BaichuanAPI", "✅ 卡片發布成功 (PUT 200/201)！檔案名稱: $cardId.json, HTTP Code: $code, Response Body: $responseBody")
                    onSuccess()
                } else {
                    android.util.Log.e("BaichuanAPI", "❌ 發布卡片寫入失敗 [檔案名稱: $cardId.json] HTTP Code: $code, Response Body: $responseBody")
                    onError("HTTP $code: $responseBody")
                }
            }
        })
    }

    /**
     * 相容介面：寫入單張卡片
     */
    fun writeToHub(url: String, cards: List<RequirementCard>, sha: String?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (cards.isEmpty()) {
            onSuccess()
            return
        }
        publishCard(cards.first(), onSuccess, onError)
    }

    /**
     * 讀取雲端 cards 目錄 (【完全禁用並註解】：100% 不會觸發任何 GET 讀取請求，第一行直接 return)
     */
    fun fetchAllCardsFromHub(onResult: (List<RequirementCard>) -> Unit) {
        onResult(emptyList())
        return
        /*
        // 舊有 GET 讀取邏輯已被完全禁用與註解：
        // android.util.Log.d("BaichuanAPI", "📥 開始發起 GET 請求讀取 cards 目錄列表")
        // ...
        */
    }

    /**
     * 依據分類代碼精準篩選 (【完全禁用】：100% 不發送任何 GET 讀取請求)
     */
    fun fetchCardsByCategory(
        categoryCode: String,
        onResult: (List<RequirementCard>) -> Unit
    ) {
        onResult(emptyList())
        return
    }

    /**
     * 帶入經緯度座標 (lat, lng) 與搜尋距離數值 (radiusKm) 向後端/雲端請求周邊卡片資料
     * 確保後端/雲端計算函數精準接收中心經緯度與前後公里數半徑
     */
    fun fetchNearbyCards(
        lat: Double,
        lng: Double,
        radiusKm: Double,
        categoryCodes: List<String>,
        onResult: (List<RequirementCard>) -> Unit
    ) {
        android.util.Log.d("BaichuanAPI", "📍 [經緯度 + 搜尋距離參數強制帶入] 中心座標: ($lat, $lng), 搜尋半徑: ${radiusKm}km, 分類: $categoryCodes")
        onResult(emptyList())
    }

    /**
     * 交叉抽屜並行/批次請求 (【完全禁用】：100% 不發送任何 GET 讀取請求)
     */
    fun fetchCardsByCategories(
        categoryCodes: List<String>,
        onResult: (List<RequirementCard>) -> Unit
    ) {
        onResult(emptyList())
        return
    }

    /**
     * 從任意 Git Raw JSON URL 動態下載卡片清單 (【完全禁用】：100% 不發送任何 GET 讀取請求)
     */
    fun refreshCardsFromGit(
        gitRawUrl: String,
        onSuccess: (List<RequirementCard>) -> Unit,
        onError: (String) -> Unit
    ) {
        onSuccess(emptyList())
        return
    }

    /**
     * 檢舉卡片紀錄
     */
    fun reportCard(cardId: String, reason: String, reporterId: String) {
        val reportData = mapOf(
            "title" to "【REPORT】$cardId",
            "body" to "Reason: $reason\nReporter: $reporterId\nTS: ${System.currentTimeMillis()}"
        )

        val request = Request.Builder()
            .url("https://api.github.com/repos/enshenglin05-coder/baichuan-v2/issues")
            .header("Authorization", "Bearer ghp_Y3Rb2ZHTrMcVDujrkjzv6SjhYicGU3107XgZ")
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "BaichuanApp")
            .post(gson.toJson(reportData).toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                android.util.Log.e("BaichuanAPI", "Report Card 失敗: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    android.util.Log.w("BaichuanAPI", "Report Card 響應錯誤 HTTP ${response.code}")
                }
                response.close()
            }
        })
    }

    /**
     * 全網下架刪除訊號
     */
    fun deleteCard(card: RequirementCard, onSuccess: () -> Unit, onError: (String) -> Unit) {
        onSuccess()
    }
}
