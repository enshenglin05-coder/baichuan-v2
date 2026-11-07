package com.example.myapplication01

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

/**
 * 後台黑名單與檢舉審核視窗管理器 (Admin Review Dialog Manager)
 * 條列顯示因「12小時內同選項被檢舉達3次」而自動隱藏並送入審核的卡片清單，
 * 支援點擊展開詳細內容，並提供「忽略」（解除）與「封鎖」（凍結）按鈕。
 */
object AdminReviewDialogManager {

    fun showAdminReviewDialog(context: Context) {
        val records = CardJsonExceptionHandler.getQuarantinedRecords()

        val scrollView = ScrollView(context).apply {
            isFillViewport = true
            setPadding(24, 24, 24, 24)
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        if (records.isEmpty()) {
            val tvEmpty = TextView(context).apply {
                text = "目前無待審核之檢舉黑名單卡片 (全數正常)"
                textSize = 14f
                setTextColor(android.graphics.Color.WHITE)
                setPadding(24, 24, 24, 24)
                gravity = Gravity.CENTER
            }
            container.addView(tvEmpty)
        } else {
            records.forEach { record ->
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

                val tvInfo = TextView(context).apply {
                    text = "ID: ${record.recordId}\n原因: ${record.reason}\n檢舉數: ${record.reportCount}\n狀態: ${record.status}"
                    textSize = 14f
                    setTextColor(android.graphics.Color.WHITE)
                    setPadding(0, 0, 0, 12)
                }
                cardLayout.addView(tvInfo)

                // 詳細內容 (預設隱藏，點擊卡片即可展開查看詳細發布內容)
                val tvDetails = TextView(context).apply {
                    text = "【詳細 Raw JSON 內容】\n${record.rawJson}"
                    textSize = 12f
                    setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
                    visibility = View.GONE
                    setPadding(0, 8, 0, 12)
                }
                cardLayout.addView(tvDetails)

                // 支援點擊卡片以展開並查看該筆卡片的詳細發布內容
                cardLayout.setOnClickListener {
                    if (tvDetails.visibility == View.VISIBLE) {
                        tvDetails.visibility = View.GONE
                    } else {
                        tvDetails.visibility = View.VISIBLE
                    }
                }

                val btnLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.END
                }

                // 「忽略」按鈕：解除檢舉 / 恢復顯示
                val btnIgnore = Button(context).apply {
                    text = "忽略"
                    textSize = 12f
                    setTextColor(android.graphics.Color.parseColor("#00FFFF"))
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setOnClickListener {
                        CardJsonExceptionHandler.updateRecordStatus(record.recordId, QuarantinedCardRecord.ReviewStatus.IGNORED)
                        Toast.makeText(context, "已忽略檢舉並放行 [${record.recordId}]", Toast.LENGTH_SHORT).show()
                        showAdminReviewDialog(context)
                    }
                }
                btnLayout.addView(btnIgnore)

                // 「封鎖」按鈕：確認違規 / 永久凍結該 ID 帳號
                val btnBlock = Button(context).apply {
                    text = "封鎖"
                    textSize = 12f
                    setTextColor(android.graphics.Color.parseColor("#FF5252"))
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setOnClickListener {
                        CardJsonExceptionHandler.updateRecordStatus(record.recordId, QuarantinedCardRecord.ReviewStatus.FROZEN)
                        Toast.makeText(context, "已永久凍結/封鎖該帳號與卡片 [${record.recordId}]", Toast.LENGTH_SHORT).show()
                        showAdminReviewDialog(context)
                    }
                }
                btnLayout.addView(btnBlock)

                cardLayout.addView(btnLayout)
                container.addView(cardLayout)
            }
        }

        scrollView.addView(container)

        AlertDialog.Builder(context)
            .setTitle("🛡️ 後台黑名單與檢舉審核視窗")
            .setView(scrollView)
            .setPositiveButton("關閉", null)
            .setNeutralButton("📦 查看已播放完畢倉庫（存棒）") { _, _ ->
                ArchivedWarehouseDialogManager.showWarehouseDialog(context)
            }
            .show()
    }
}
