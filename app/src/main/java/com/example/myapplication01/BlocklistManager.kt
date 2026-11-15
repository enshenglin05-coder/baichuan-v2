package com.example.myapplication01

import java.util.concurrent.ConcurrentHashMap

/**
 * 專案核心架構與資料流防線規範 (Native Serverless Architecture)
 * 1. 傳送與拉取攔截 (Blocklist Enforcement):
 *    - 系統在每次進行資料傳送（發布）或拉取（接收）前，必須即時比對封鎖名單（Blocklist）。
 *    - 偵測命中黑名單之 ID 者，強制禁止傳送與拉取，直接阻斷。
 */
object BlocklistManager {

    private val blockedIds = ConcurrentHashMap.newKeySet<String>()

    /**
     * 新增封鎖 ID (如發布者裝置 ID 或卡片 ID)
     */
    fun block(id: String) {
        if (id.isNotBlank()) {
            blockedIds.add(id)
            try {
                android.util.Log.w("Blocklist", "🚫 已將 ID 納入黑名單封鎖: $id")
            } catch (_: Throwable) {
                println("[Blocklist] 🚫 已將 ID 納入黑名單封鎖: $id")
            }
        }
    }

    /**
     * 解除封鎖 ID
     */
    fun unblock(id: String) {
        blockedIds.remove(id)
    }

    /**
     * 檢查標的 ID 是否在黑名單中
     */
    fun isBlocked(id: String?): Boolean {
        if (id.isNullOrBlank()) return false
        return blockedIds.contains(id)
    }

    /**
     * 傳送 (發布) 攔截比對：偵測發布者 ID 或卡片 ID 是否命中黑名單
     */
    fun isSendBlocked(publisherDeviceId: String?, cardId: String?): Boolean {
        return isBlocked(publisherDeviceId) || isBlocked(cardId)
    }

    /**
     * 拉取 (接收) 攔截比對：偵測接收封包發布者 ID 或卡片 ID 是否命中黑名單
     */
    fun isPullBlocked(publisherDeviceId: String?, cardId: String?): Boolean {
        return isBlocked(publisherDeviceId) || isBlocked(cardId)
    }

    /**
     * 清空封鎖名單 (測試隔離使用)
     */
    fun clearAll() {
        blockedIds.clear()
    }

    /**
     * 取得當前所有封鎖 ID
     */
    fun getBlockedIds(): Set<String> {
        return blockedIds.toSet()
    }
}
