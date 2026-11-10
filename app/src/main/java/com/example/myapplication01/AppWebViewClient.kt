package com.example.myapplication01

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun dialNumber(phoneNumber: String) {
        // 純本地 Intent 喚醒，完全不需要連網
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            PhoneActionHelper.openDialer(context, phoneNumber)
        }
    }

    @JavascriptInterface
    fun makeCall(phoneNumber: String?) {
        if (!phoneNumber.isNullOrEmpty()) {
            dialNumber(phoneNumber)
        }
    }
}

class AppWebViewClient(private val context: Context) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        var url = request?.url?.toString() ?: return false
        if (url == "#" || url.endsWith("/#")) {
            return true
        }
        if (url.startsWith("http://", ignoreCase = true)) {
            url = "https://" + url.substring(7)
        }
        if (handleTelUrl(view, url) || handleExternalAppUrl(view, url)) {
            return true // ⚠️ 必須回傳 true，告訴系統「原生已接管，停止 WebView 繼續加載與搜尋」
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    // 舊版 API 兼容
    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        if (url == "#" || url?.endsWith("/#") == true) {
            return true
        }
        if (handleTelUrl(view, url) || handleExternalAppUrl(view, url)) {
            return true
        }
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)

        // 💥 強制攔截：只要網址裡面含 tel:，不論是不是 Google 搜尋網址，立刻砍掉該網路請求！
        if (url.contains("tel:", ignoreCase = true) || url.contains("tel%3a", ignoreCase = true)) {
            view?.post {
                handleTelUrl(view, url)
            }
            return WebResourceResponse("text/plain", "UTF-8", null)
        }

        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
        // 💥 頁面一開始載入，如果是帶有 tel: 的 google 搜尋，馬上 stopLoading() 並撥號
        if (url != null && (url.contains("tel:", ignoreCase = true) || url.contains("tel%3a", ignoreCase = true))) {
            view?.stopLoading()
            handleTelUrl(view, url)
            return
        }
        super.onPageStarted(view, url, favicon)
    }

    /**
     * 核心撥號攔截邏輯：完全防封死 (強效雙重檢查 + 乾淨純數字提取)
     */
    fun handleTelUrl(view: WebView?, rawUrl: String?): Boolean {
        if (rawUrl.isNullOrEmpty()) return false

        // 1. URL 解碼 (防止 %3A 或 Google 搜尋包覆)
        val decodedUrl = try {
            java.net.URLDecoder.decode(rawUrl, "UTF-8")
        } catch (_: Exception) {
            rawUrl
        }

        // 2. 雙重檢查：不管是純 tel: 還是已經被轉成 Google 搜尋的網址，一律強制抓取號碼！
        if (decodedUrl.contains("tel:", ignoreCase = true) ||
            decodedUrl.contains("tel%3a", ignoreCase = true) ||
            decodedUrl.contains("google.com/search?q=tel:", ignoreCase = true)
        ) {
            val rawExtracted = PhoneActionHelper.extractPhoneNumberFromUrl(decodedUrl)
            val pureNumber = if (rawExtracted.isNotBlank()) {
                rawExtracted
            } else {
                decodedUrl.substringAfter("tel:", "").filter { it.isDigit() }
            }

            if (pureNumber.isNotEmpty()) {
                view?.stopLoading() // 立即強制終止 WebView 載入 Google 網頁

                // 喚醒原生撥號盤
                val ctx = view?.context ?: context
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$pureNumber")).apply {
                    if (ctx !is android.app.Activity) {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                try {
                    ctx.startActivity(intent)
                } catch (_: Exception) {
                    PhoneActionHelper.openDialer(ctx, pureNumber)
                }
                return true // 回傳 true 告知系統已完全攔截
            }
        }
        return false
    }

    /**
     * 綜合外部 App URL / 深層連結 (Deep Link) 攔截器 (1-2-3-4 實體索引對齊)
     */
    fun handleExternalAppUrl(view: WebView?, rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank() || rawUrl == "#") return false

        val decodedUrl = try { java.net.URLDecoder.decode(rawUrl, "UTF-8") } catch (_: Exception) { rawUrl }

        // 位置 3：LINE App 喚醒攔截 (line.me / line://)
        if (decodedUrl.contains("line.me", ignoreCase = true) || decodedUrl.startsWith("line://", ignoreCase = true)) {
            return launchExternalIntent(view, decodedUrl)
        }

        // 位置 3 輔助：Telegram App 喚醒攔截 (t.me / tg://)
        if (decodedUrl.contains("t.me", ignoreCase = true) || decodedUrl.startsWith("tg://", ignoreCase = true)) {
            return launchExternalIntent(view, decodedUrl)
        }

        // 位置 1：Google 地圖導航喚醒 (google.com/maps)
        if (decodedUrl.contains("google.com/maps", ignoreCase = true) || decodedUrl.startsWith("geo:", ignoreCase = true)) {
            return launchExternalIntent(view, decodedUrl)
        }

        // 位置 4：個人 / 產品介紹與外部網址攔截 (http:// 或 https://)
        if (decodedUrl.startsWith("http://", ignoreCase = true) || decodedUrl.startsWith("https://", ignoreCase = true)) {
            return launchExternalIntent(view, decodedUrl)
        }

        return false
    }

    private fun launchExternalIntent(view: WebView?, url: String): Boolean {
        try {
            view?.stopLoading() // 立即中斷 WebView 內網頁載入
            var secureUrl = url.trim()
            if (secureUrl.startsWith("http://", ignoreCase = true)) {
                secureUrl = "https://" + secureUrl.substring(7)
            }
            val ctx = view?.context ?: context
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(secureUrl)).apply {
                if (ctx !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            ctx.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun handleTelUri(view: WebView?, rawUrl: String?): Boolean {
        return handleTelUrl(view, rawUrl)
    }
}
