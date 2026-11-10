package com.example.myapplication01

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * ContactUtils：聯絡工具類，負責處理通訊軟體跳轉與剪貼簿操作
 */
object ContactUtils {

    /**
     * 徹底清理污染符號 (去除前後空白、外層括號、開頭波浪號 ~ 與 @ 符號)
     */
    fun cleanContactValue(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var cleaned = input.trim()
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            cleaned = cleaned.substring(1, cleaned.length - 1).trim()
        }
        cleaned = cleaned.removePrefix("~").removePrefix("@").trim()
        if (cleaned.startsWith("(") && cleaned.endsWith(")")) {
            cleaned = cleaned.substring(1, cleaned.length - 1).trim()
        }
        return cleaned
    }

    /**
     * 清理與格式化 LINE ID (去除前後空白與開頭波浪號 ~ 與 @ 符號)
     */
    fun cleanLineId(lineId: String): String {
        return cleanContactValue(lineId)
    }

    /**
     * 喚起 LINE 搜尋好友介面
     */
    fun openLineSearch(context: Context, lineId: String) {
        try {
            val cleanedId = cleanLineId(lineId)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://line.me/ti/p/~$cleanedId"))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "未安裝 LINE 應用程式", Toast.LENGTH_SHORT).show()
        }
    }

    fun cleanPhoneHeader(rawText: String): String {
        // 移除 Gemini 灌上的 http://, https://, tel: 等網路抬頭
        return rawText.replace("http://", "", ignoreCase = true)
                      .replace("https://", "", ignoreCase = true)
                      .replace("tel:", "", ignoreCase = true)
                      .replace(Regex("[^0-9]"), "") // 確保只留純號碼
    }

    /**
     * 格式化電話號碼 (去除非數字以外的字元，移除網路抬頭)
     */
    fun cleanPhoneNumber(phoneNumber: String): String {
        return cleanPhoneHeader(phoneNumber)
    }

    /**
     * 觸發系統原生撥號盤撥打電話 (統一電話撥打與防呆規範)
     */
    fun makePhoneCall(context: Context, rawPhone: String?) {
        try {
            val phone = rawPhone?.trim() ?: ""
            if (phone.isEmpty() || phone == "null") {
                Toast.makeText(context.applicationContext, "此卡片未提供有效電話號碼", Toast.LENGTH_SHORT).show()
                return
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.applicationContext.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context.applicationContext, "無法開啟撥號盤", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 嘗試開啟特定包名的應用程式
     */
    fun openApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "未安裝該應用程式", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "開啟應用程式失敗", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 複製 ID 或任何文字至剪貼簿
     */
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Contact ID", text)
        clipboard.setPrimaryClip(clip)
        val toastMsg = if (text.length > 50) "已複製至剪貼簿 (長度: ${text.length})" else "已複製 ID: $text"
        Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
    }
}
