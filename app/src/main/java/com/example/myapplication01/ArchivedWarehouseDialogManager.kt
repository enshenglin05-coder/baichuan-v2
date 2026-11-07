package com.example.myapplication01

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

/**
 * 已播放完畢倉庫（存棒）與人工覆核按鍵管理器 (Archived Warehouse Dialog Manager)
 * 提供已歸零卡片的完整歷史紀錄查核、爭議佐證，並配置極簡兩大專屬覆核控制按鍵：
 * 1. 【刪除按鍵】：人工手動點擊後，徹底將該筆卡片資料從存棒中銷毀／移除。
 * 2. 【保存按鍵】：將該筆卡片保留在存棒中持續歸檔，符合數據保留合約規範。
 */
object ArchivedWarehouseDialogManager {

    data class ArchivedWarehouseItem(
        val cardId: String,
        val text: String,
        val latitude: Double,
        val longitude: Double,
        val initialTotalCount: Int = 10,
        val finalRemainingCount: Int = 0,
        val archivedAt: String = "",
        val isRetained: Boolean = false
    )

    /**
     * 讀取並整合所有已歸零之存棒倉庫卡片清單
     */
    fun getWarehouseItems(context: Context? = null): List<ArchivedWarehouseItem> {
        val itemsMap = mutableMapOf<String, ArchivedWarehouseItem>()

        // 1. 從 LocalCardDatabase 載入 archived_cards/ 實體檔案
        val archivedCards = LocalCardDatabase.getArchivedCards(context)
        archivedCards.forEach { card ->
            itemsMap[card.card_id] = ArchivedWarehouseItem(
                cardId = card.card_id,
                text = card.description.ifBlank { card.category_tag },
                latitude = card.gps_lat,
                longitude = card.gps_lng,
                initialTotalCount = 10,
                finalRemainingCount = 0,
                archivedAt = TTSAdModel.getCurrentIsoTimestamp(),
                isRetained = card.moderation_status == "RETAINED_VERIFIED"
            )
        }

        // 2. 從 LocalTtsAdDatabase 載入 local_recycled_records.json 回收紀錄
        val recycledRecords = LocalTtsAdDatabase.loadRecycledRecords(context)
        recycledRecords.forEach { record ->
            val existing = itemsMap[record.adId]
            if (existing != null) {
                itemsMap[record.adId] = existing.copy(
                    initialTotalCount = record.initialTotalCount,
                    finalRemainingCount = record.finalRemainingCount,
                    archivedAt = record.archivedAt
                )
            } else {
                itemsMap[record.adId] = ArchivedWarehouseItem(
                    cardId = record.adId.ifBlank { record.archiveId },
                    text = record.text,
                    latitude = record.location.latitude,
                    longitude = record.location.longitude,
                    initialTotalCount = record.initialTotalCount,
                    finalRemainingCount = record.finalRemainingCount,
                    archivedAt = record.archivedAt
                )
            }
        }

        return itemsMap.values.toList()
    }

    /**
     * 顯示「已播放完畢倉庫（存棒）」彈出視窗介面
     */
    fun showWarehouseDialog(context: Context) {
        val items = getWarehouseItems(context)

        val scrollView = ScrollView(context).apply {
            isFillViewport = true
            setPadding(24, 24, 24, 24)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        // 標題與簡介說明
        val tvHeader = TextView(context).apply {
            text = "已播放完畢卡片存棒歸檔總數：${items.size} 筆"
            textSize = 14f
            setTextColor(Color.parseColor("#00FFC8"))
            setPadding(8, 8, 8, 20)
        }
        container.addView(tvHeader)

        if (items.isEmpty()) {
            val tvEmpty = TextView(context).apply {
                text = "📦 目前存棒倉庫中尚無已播放完畢之歸零卡片。"
                textSize = 14f
                setTextColor(Color.WHITE)
                setPadding(24, 36, 24, 36)
                gravity = Gravity.CENTER
            }
            container.addView(tvEmpty)
        } else {
            items.forEach { item ->
                val cardLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(24, 24, 24, 24)
                    background = context.getDrawable(R.drawable.bg_mist_card)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 20
                    }
                }

                val statusText = if (item.isRetained) "【已確認保存歸檔】" else "【待人工覆核】"
                val statusColorInt = if (item.isRetained) Color.parseColor("#4CAF50") else Color.parseColor("#FFB74D")

                val tvInfo = TextView(context).apply {
                    text = "🆔 卡片 ID: ${item.cardId}\n" +
                           "📢 播報內容: '${item.text}'\n" +
                           "📍 目標座標: (${item.latitude}, ${item.longitude})\n" +
                           "📊 播放計數: 初始 ${item.initialTotalCount} 次 ➔ 最終 ${item.finalRemainingCount} 次 (已歸零)"
                    textSize = 13f
                    setTextColor(Color.WHITE)
                    setPadding(0, 0, 0, 8)
                }
                cardLayout.addView(tvInfo)

                val tvStatus = TextView(context).apply {
                    text = "🏷️ 存棒狀態: $statusText"
                    textSize = 13f
                    setTextColor(statusColorInt)
                    setPadding(0, 0, 0, 12)
                }
                cardLayout.addView(tvStatus)

                // 兩大專屬覆核控制按鍵列
                val btnLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.END
                }

                // 1. 【保存按鍵】：將筆卡片保留在存棒中持續歸檔
                val btnSave = Button(context).apply {
                    text = "保存"
                    textSize = 12f
                    setTextColor(Color.parseColor("#4CAF50"))
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        LocalCardDatabase.retainArchivedCard(item.cardId, context)
                        Toast.makeText(context, "已確認保存卡片 [${item.cardId}] 於存棒倉庫中", Toast.LENGTH_SHORT).show()
                        showWarehouseDialog(context)
                    }
                }
                btnLayout.addView(btnSave)

                // 2. 【刪除按鍵】：徹底將該筆卡片資料從存棒中銷毀／移除
                val btnDelete = Button(context).apply {
                    text = "刪除"
                    textSize = 12f
                    setTextColor(Color.parseColor("#FF5252"))
                    setBackgroundColor(Color.TRANSPARENT)
                    setOnClickListener {
                        LocalCardDatabase.deleteArchivedCard(item.cardId, context)
                        LocalTtsAdDatabase.deleteRecycledRecord(item.cardId, context)
                        Toast.makeText(context, "已徹底銷毀並移除存棒卡片 [${item.cardId}]", Toast.LENGTH_SHORT).show()
                        showWarehouseDialog(context)
                    }
                }
                btnLayout.addView(btnDelete)

                cardLayout.addView(btnLayout)
                container.addView(cardLayout)
            }
        }

        scrollView.addView(container)

        AlertDialog.Builder(context)
            .setTitle("📦 已播放完畢倉庫（存棒）")
            .setView(scrollView)
            .setPositiveButton("關閉", null)
            .show()
    }
}
