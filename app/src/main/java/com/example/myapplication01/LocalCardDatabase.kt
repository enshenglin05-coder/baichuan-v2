package com.example.myapplication01

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * 本地端 JSON 序列化資料庫與卡片持久化儲存元件 (Local JSON Database Manager)
 * 負責將使用者手動輸入的卡片轉為標準結構化 JSON（對應 12大項97格、經緯度、A/B 交叉標記與 time 時間戳記），
 * 寫入在地實體資料庫 (.json 檔案)，並提供遠端 GitHub PUT 上傳通道讀取使用。
 */
object LocalCardDatabase {

    private var dbDirectory: File? = null
    private val gson: Gson get() = BaichuanApiService.gson

    private fun safeLogD(tag: String, msg: String) {
        try {
            android.util.Log.d(tag, msg)
        } catch (e: Throwable) {
            println("[$tag] $msg")
        }
    }

    private fun safeLogE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            if (tr != null) {
                android.util.Log.e(tag, msg, tr)
            } else {
                android.util.Log.e(tag, msg)
            }
        } catch (e: Throwable) {
            println("[$tag] $msg ${tr?.message ?: ""}")
        }
    }

    /**
     * 初始化本地端卡片 JSON 資料庫目錄 (`/data/data/<package>/files/local_cards/`)
     */
    fun init(context: Context) {
        val baseDir = context.applicationContext.filesDir
        val cardsDir = File(baseDir, "local_cards")
        if (!cardsDir.exists()) {
            cardsDir.mkdirs()
        }
        dbDirectory = cardsDir
        safeLogD("LocalCardDB", "📁 本地端卡片 JSON 資料庫初始化完成：${cardsDir.absolutePath}")
    }

    private fun getDbDirectory(context: Context? = null): File {
        dbDirectory?.let { return it }
        val ctx = context?.applicationContext
        val baseDir = ctx?.filesDir ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp", "local_cards_temp")
        val cardsDir = File(baseDir, "local_cards")
        if (!cardsDir.exists()) {
            cardsDir.mkdirs()
        }
        dbDirectory = cardsDir
        return cardsDir
    }

    /**
     * 1. 本地 JSON 序列化與資料庫實體寫入：
     * 將手動發布之卡片轉為標準結構化 JSON (對應 12大項97格、經緯度、A/B 交叉標記與 time 時間戳記)
     * 並將其存入在地資料庫實體檔案 (`$cardId.json`)
     */
    fun saveCard(card: RequirementCard, context: Context? = null): File? {
        return try {
            val directory = getDbDirectory(context)
            
            // 覆蓋/去重原則：刪除同一抽屜 (category_code + drawer_type) 或同一發布者的舊卡片檔案，避免無限累積 #03, #04, #05
            val files = directory.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) }
            files?.forEach { file ->
                try {
                    val jsonStr = file.readText(Charsets.UTF_8)
                    val existing = gson.fromJson(jsonStr, RequirementCard::class.java)
                    if (existing != null) {
                        val isSameDrawer = existing.category_code == card.category_code && existing.drawer_type == card.drawer_type
                        val isSamePublisher = card.publisher_device_id.isNotBlank() && existing.publisher_device_id == card.publisher_device_id
                        val isSameId = existing.card_id == card.card_id
                        if (isSameDrawer || isSamePublisher || isSameId) {
                            file.delete()
                            safeLogD("LocalCardDB", "🧹 [覆蓋舊卡片] 已刪除同抽屜/同發布者舊卡片檔案: ${file.name}")
                        }
                    }
                } catch (e: Exception) {
                    // ignore parse error on cleanup
                }
            }

            val file = File(directory, "${card.card_id}.json")
            val jsonString = gson.toJson(card)
            file.writeText(jsonString, Charsets.UTF_8)
            
            safeLogD("LocalCardDB", "💾 [在地資料庫] 成功儲存最新覆蓋卡片: ${file.name} (抽屜: ${card.category_code} / ${card.drawer_type})")
            file
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "❌ [在地資料庫] 序列化寫入失敗: ${e.message}", e)
            null
        }
    }

    /**
     * 2. 雲端上傳通道對接資料提煉：
     * 從在地 JSON 資料庫讀取該筆卡片的標準 JSON 檔案字串 (提供 PUT 上傳遠端 GitHub cards/ 使用)
     */
    fun getCardJson(cardId: String, context: Context? = null): String? {
        return try {
            val directory = getDbDirectory(context)
            val file = File(directory, "$cardId.json")
            if (file.exists()) {
                file.readText(Charsets.UTF_8)
            } else {
                null
            }
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "❌ 讀取在地 JSON 資料庫失敗 [$cardId]: ${e.message}")
            null
        }
    }

    /**
     * 從在地 JSON 資料庫反序列化讀取 RequirementCard 物件
     */
    fun getCard(cardId: String, context: Context? = null): RequirementCard? {
        val jsonStr = getCardJson(cardId, context) ?: return null
        return try {
            gson.fromJson(jsonStr, RequirementCard::class.java)
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "❌ 反序列化在地卡片失敗 [$cardId]: ${e.message}")
            null
        }
    }

    /**
     * 讀取在地 JSON 資料庫中所有手動發布之卡片清單 (自動以抽屜去重，保留最新一筆)
     */
    fun getAllLocalCards(context: Context? = null): List<RequirementCard> {
        val directory = getDbDirectory(context)
        val files = directory.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) } ?: return emptyList()
        
        val cardMap = mutableMapOf<String, RequirementCard>()
        for (file in files) {
            try {
                val jsonStr = file.readText(Charsets.UTF_8)
                val card = gson.fromJson(jsonStr, RequirementCard::class.java)
                if (card != null && card.card_id.isNotBlank()) {
                    val drawerKey = "${card.category_code}_${card.drawer_type}"
                    val existing = cardMap[drawerKey]
                    if (existing == null || card.created_at > existing.created_at) {
                        cardMap[drawerKey] = card
                    }
                }
            } catch (e: Exception) {
                safeLogE("LocalCardDB", "解析在地卡片檔失敗 [${file.name}]: ${e.message}")
            }
        }
        return cardMap.values.sortedByDescending { it.created_at }
    }

    /**
     * 清除本地端所有超過 12 小時生命週期的過期卡片 JSON 檔案
     */
    fun purgeExpiredLocalCards(context: Context? = null) {
        try {
            val directory = getDbDirectory(context)
            val files = directory.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) } ?: return
            val now = System.currentTimeMillis()
            for (file in files) {
                try {
                    val jsonStr = file.readText(Charsets.UTF_8)
                    val card = gson.fromJson(jsonStr, RequirementCard::class.java)
                    if (card != null && card.isExpired(now)) {
                        file.delete()
                        safeLogD("LocalCardDB", "🧹 [12小時本地自動抹除] 已刪除過期卡片實體檔案: ${file.name}")
                    }
                } catch (e: Exception) {
                    // ignore parse error on cleanup
                }
            }
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "❌ 清除本地過期卡片檔案失敗: ${e.message}")
        }
    }

    /**
     * 從在地 JSON 資料庫移除指定卡片
     */
    fun deleteCard(cardId: String, context: Context? = null): Boolean {
        return try {
            val directory = getDbDirectory(context)
            val file = File(directory, "$cardId.json")
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "刪除在地卡片檔失敗 [$cardId]: ${e.message}")
            false
        }
    }

    /**
     * 清空在地 JSON 資料庫 (隔離測試使用)
     */
    fun clearAll(context: Context? = null) {
        try {
            val directory = getDbDirectory(context)
            directory.listFiles()?.forEach { it.delete() }

            val baseDir = directory.parentFile ?: context?.filesDir
            val archiveDirectory = File(baseDir, "archived_cards")
            if (archiveDirectory.exists()) {
                archiveDirectory.listFiles()?.forEach { it.delete() }
            }
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "清空在地卡片資料庫失敗: ${e.message}")
        }
    }

    /**
     * 歸零自動搬運（屍體歸位）：
     * 當卡片播放次數扣減至 0 歸零時，實體將卡片檔案從主資料夾 (`local_cards/`) 搬移/搬運至已封存/回收倉庫 (`archived_cards/`)
     */
    fun archiveCardFile(cardId: String, context: Context? = null): File? {
        return try {
            val mainDirectory = getDbDirectory(context)
            val sourceFile = File(mainDirectory, "$cardId.json")
            if (!sourceFile.exists()) {
                safeLogD("LocalCardDB", "⚠️ 找不到主資料夾卡片檔案 [$cardId.json]，可能已移走或不存在")
                return null
            }

            val baseDir = mainDirectory.parentFile ?: context?.filesDir
            val archiveDirectory = File(baseDir, "archived_cards")
            if (!archiveDirectory.exists()) {
                archiveDirectory.mkdirs()
            }

            val destFile = File(archiveDirectory, "$cardId.json")
            val moved = sourceFile.renameTo(destFile)
            if (!moved) {
                // 若跨檔案系統重命名失敗，則採用複製後刪除
                destFile.writeBytes(sourceFile.readBytes())
                sourceFile.delete()
            }

            safeLogD(
                "LocalCardDB",
                "📦 [歸零自動搬運/屍體歸位] 成功將卡片 [$cardId] 實體檔案從 local_cards/ 搬移至專屬回收倉庫: ${destFile.absolutePath}"
            )
            destFile
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "❌ 搬移歸零卡片至回收倉庫失敗 [$cardId]: ${e.message}", e)
            null
        }
    }

    /**
     * 讀取已封存/回收倉庫 (`archived_cards/`) 中所有已歸零搬運之卡片清單
     */
    fun getArchivedCards(context: Context? = null): List<RequirementCard> {
        return try {
            val directory = getDbDirectory(context)
            val baseDir = directory.parentFile ?: context?.filesDir
            val archiveDirectory = File(baseDir, "archived_cards")
            if (!archiveDirectory.exists()) return emptyList()

            val files = archiveDirectory.listFiles { _, name -> name.endsWith(".json", ignoreCase = true) } ?: return emptyList()
            val archivedList = mutableListOf<RequirementCard>()
            for (file in files) {
                try {
                    val jsonStr = file.readText(Charsets.UTF_8)
                    val card = gson.fromJson(jsonStr, RequirementCard::class.java)
                    if (card != null) {
                        archivedList.add(card)
                    }
                } catch (e: Exception) {
                    safeLogE("LocalCardDB", "解析已封存卡片檔失敗 [${file.name}]: ${e.message}")
                }
            }
            archivedList
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "讀取已封存卡片倉庫失敗: ${e.message}")
            emptyList()
        }
    }

    /**
     * 【存棒倉庫控制 - 刪除按鍵】：徹底從已封存回收倉庫 (`archived_cards/`) 中銷毀刪除檔案
     */
    fun deleteArchivedCard(cardId: String, context: Context? = null): Boolean {
        return try {
            val directory = getDbDirectory(context)
            val baseDir = directory.parentFile ?: context?.filesDir
            val archiveDirectory = File(baseDir, "archived_cards")
            val file = File(archiveDirectory, "$cardId.json")
            if (file.exists()) {
                val deleted = file.delete()
                safeLogD("LocalCardDB", "🗑️ [存棒銷毀] 已徹底刪除封存倉庫實體卡片檔: ${file.name}")
                deleted
            } else {
                true
            }
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "❌ 銷毀封存卡片檔失敗 [$cardId]: ${e.message}")
            false
        }
    }

    /**
     * 【存棒倉庫控制 - 保存按鍵】：將封存卡片標記為確認保存狀態 (RETAINED_VERIFIED) 歸檔
     */
    fun retainArchivedCard(cardId: String, context: Context? = null): Boolean {
        return try {
            val directory = getDbDirectory(context)
            val baseDir = directory.parentFile ?: context?.filesDir
            val archiveDirectory = File(baseDir, "archived_cards")
            val file = File(archiveDirectory, "$cardId.json")
            if (file.exists()) {
                val jsonStr = file.readText(Charsets.UTF_8)
                val card = gson.fromJson(jsonStr, RequirementCard::class.java)
                if (card != null) {
                    val updatedCard = card.copy(moderation_status = "RETAINED_VERIFIED")
                    file.writeText(gson.toJson(updatedCard), Charsets.UTF_8)
                    safeLogD("LocalCardDB", "💾 [存棒保存] 已更新並永久保存封存倉庫卡片檔: ${file.name}")
                    return true
                }
            }
            false
        } catch (e: Exception) {
            safeLogE("LocalCardDB", "❌ 保存封存卡片檔失敗 [$cardId]: ${e.message}")
            false
        }
    }
}
