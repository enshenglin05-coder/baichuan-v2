package com.example.myapplication01

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * 獨立 AI 模態全螢幕頁面 (UI4 AiModalityActivity)
 * 精準修飾：拉開卡片間距、卡片邊框植入 128 色流彩邊框、抽掉開啟按鈕底座背景
 */
class AiModalityActivity : BaseActivity() {

    private lateinit var topBarAiModality: View
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 同步當前語系
        val langCode = GlobalState.selectedLanguageCode
        val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(langCode)
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_ai_modality)

        initViews()
        handleWindowInsets()
        setupListeners()
        setupRainbowEngine()
        populateModalityCards()
    }

    private fun initViews() {
        topBarAiModality = findViewById(R.id.topBarAiModality)
        btnBack = findViewById(R.id.btnBackAiModality)
        tvTitle = findViewById(R.id.tvAiModalityTitle)
        container = findViewById(R.id.containerAiModality)
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayoutAiModality)) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            topBarAiModality.updatePadding(top = insets.top + (resources.displayMetrics.density * 8).toInt())
            windowInsets
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRainbowEngine() {
        val rootLayout = findViewById<FrameLayout>(R.id.rootLayoutAiModality)
        val composeView = ComposeView(this).apply {
            isClickable = false
            isFocusable = false
            setContent {
                Box(modifier = Modifier.fillMaxSize()) {
                    LightAppFullscreenAtmosphere()
                    LightAppSuiteBox()
                }
            }
        }
        rootLayout.addView(composeView, 0, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
    }

    private fun populateModalityCards() {
        val langCode = GlobalState.selectedLanguageCode
        val dialogTitle = when (langCode) {
            "en" -> "🚀 AI Modality Hub"
            "ja" -> "🚀 AI モダリティハブ"
            "ko" -> "🚀 AI 모달리티 허브"
            "vi" -> "🚀 Trung tâm phương thức AI"
            else -> "🚀 AI 模態中心"
        }
        tvTitle.text = dialogTitle

        val instructionText = when (langCode) {
            "en" -> "Tap the 'Open' button on the right to launch the AI application with an empty input buffer for manual paste."
            "ja" -> "右側の「開く」ボタンをタップして、空の入力枠で AI アプリケーションを起動し、手動で貼り付けます。"
            "ko" -> "오른쪽 '열기' 버튼을 눌러 빈 대화 상자로 AI 애플리케이션을 실행하고 수동으로 붙여넣으세요."
            "vi" -> "Nhấn nút 'Mở' ở bên phải để mở ứng dụng AI với ô nhập liệu trống, sẵn sàng dán thủ công."
            else -> "請點擊右側「開啟」按鈕跳轉至 AI 應用程式，對話框將保持完全空白，以利手動貼上草稿"
        }
        val resourceHeaderTitle = when (langCode) {
            "en" -> "Resource Items in Page"
            "ja" -> "ページ内のリソース項目"
            "ko" -> "페이지 내 리소스 항목"
            "vi" -> "Mục tài nguyên trong trang"
            else -> "頁面中資源項目"
        }
        val resources = when (langCode) {
            "en" -> listOf("Google Map Navigation", "Product Details", "Contact Communication", "Direct Phone Call")
            "ja" -> listOf("Googleマップ位置ジャンプ", "製品説明ジャンプ", "連絡通信ジャンプ", "電話発信ジャンプ")
            "ko" -> listOf("Google 지도 위치 이동", "제품 설명 이동", "연락처 통신 이동", "전화 걸기 이동")
            "vi" -> listOf("Chuyển hướng Google Map", "Chi tiết sản phẩm", "Kết nối liên lạc", "Gọi điện thoại trực tiếp")
            else -> listOf("Google Map 位置跳轉", "產品說明跳轉", "聯絡通訊跳轉", "撥打電話跳轉")
        }
        val geminiWarning = when (langCode) {
            "en" -> "⚠️ Direct calling inside page is not supported; please copy to dial."
            "ja" -> "⚠️ ページ内からの直接発信はサポートされていません。コピーして発信してください。"
            "ko" -> "⚠️ 페이지 내 직접 통화는 지원되지 않으며, 복사 후 통화해야 합니다."
            "vi" -> "⚠️ Không hỗ trợ gọi trực tiếp trong trang, vui lòng sao chép để gọi."
            else -> "⚠️ 不支援頁面內跳轉撥打電話，需複製後再進行撥打"
        }
        val openButtonText = when (langCode) {
            "en" -> "Open"
            "ja" -> "開く"
            "ko" -> "열기"
            "vi" -> "Mở"
            else -> "開啟"
        }

        val modalities = AiModalityDialogManager.getModalities(langCode)

        // 建立提示文字
        val instructionView = TextView(this).apply {
            text = instructionText
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(24, 16, 24, 24)
            gravity = Gravity.CENTER
        }
        container.addView(instructionView)

        modalities.forEach { modality ->
            // 卡片容器：適度拉開上下／左右間距 (Margin)
            val cardFrame = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 26
                    leftMargin = 16
                    rightMargin = 16
                }
                background = getDrawable(R.drawable.bg_mist_card)
            }

            val cardContentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(24, 24, 24, 24)
            }

            val cardMainLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val textContentLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f)
            }

            val tvCardTitle = TextView(this).apply {
                text = "${modality.icon} ${modality.title}"
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.WHITE)
                setPadding(0, 0, 0, 4)
            }
            textContentLayout.addView(tvCardTitle)

            val tvSub = TextView(this).apply {
                text = modality.subtitle
                textSize = 12f
                setTextColor(android.graphics.Color.parseColor("#CCCCCC"))
                setPadding(0, 0, 0, 8)
            }
            textContentLayout.addView(tvSub)

            val indentPx = (16 * this.resources.displayMetrics.density).toInt()

            val tvResourceHeader = TextView(this).apply {
                text = resourceHeaderTitle
                textSize = 13f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#FFCC00"))
                setPadding(indentPx, 4, 0, 4)
            }
            textContentLayout.addView(tvResourceHeader)

            resources.forEach { res ->
                val tvRes = TextView(this).apply {
                    textSize = 12f
                    setPadding(indentPx, 4, 0, 4)
                }

                val bulletPrefix = "✦ "
                val fullText = "$bulletPrefix$res"

                val electronicColors = intArrayOf(
                    android.graphics.Color.parseColor("#FF2222"), // 紅
                    android.graphics.Color.parseColor("#FF8800"), // 橙
                    android.graphics.Color.parseColor("#FFFF00"), // 黃
                    android.graphics.Color.parseColor("#00FF66"), // 綠
                    android.graphics.Color.parseColor("#00FFFF"), // 青
                    android.graphics.Color.parseColor("#3366FF"), // 藍
                    android.graphics.Color.parseColor("#CC33FF")  // 紫
                )

                val handler = android.os.Handler(android.os.Looper.getMainLooper())
                val runnable = object : Runnable {
                    override fun run() {
                        if (tvRes.windowToken == null) return
                        val randomColor = electronicColors.random()
                        val spannable = android.text.SpannableString(fullText).apply {
                            setSpan(
                                android.text.style.ForegroundColorSpan(randomColor),
                                0,
                                bulletPrefix.length,
                                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            setSpan(
                                android.text.style.ForegroundColorSpan(android.graphics.Color.WHITE),
                                bulletPrefix.length,
                                fullText.length,
                                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        tvRes.text = spannable
                        val nextDelay = (80..250).random().toLong()
                        handler.postDelayed(this, nextDelay)
                    }
                }

                tvRes.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        handler.post(runnable)
                    }
                    override fun onViewDetachedFromWindow(v: View) {
                        handler.removeCallbacks(runnable)
                    }
                })

                textContentLayout.addView(tvRes)
            }

            if (modality.id == "GEMINI") {
                val tvNote = TextView(this).apply {
                    text = geminiWarning
                    textSize = 11f
                    setTextColor(android.graphics.Color.parseColor("#FF5252"))
                    setPadding(indentPx, 8, 0, 0)
                }
                textContentLayout.addView(tvNote)
            }

            cardMainLayout.addView(textContentLayout)

            // 「開啟」按鈕：抽掉底座背景（透明化），讓按鈕簡潔融合於卡片中
            val btnOpen = Button(this).apply {
                text = openButtonText
                textSize = 14f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setTextColor(android.graphics.Color.parseColor("#00FFFF"))
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    leftMargin = 16
                }
                setOnClickListener {
                    try {
                        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val copiedText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                        
                        // 確保透過 ClipboardManager 確實寫入系統剪貼簿
                        if (copiedText.isNotBlank()) {
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Baichuan Pack", copiedText))
                        }
                        Toast.makeText(this@AiModalityActivity, "已帶入剪貼簿", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@AiModalityActivity, "無法寫入剪貼簿", Toast.LENGTH_SHORT).show()
                    }

                    // 啟動對應 AI 應用程式或網頁版 (含 ActivityNotFoundException 防錯與 Web 備援)
                    val packageName = when (modality.id) {
                        "ChatGPT" -> "com.openai.chatgpt"
                        "GEMINI" -> "com.google.android.apps.bard"
                        "CLAUDE" -> "com.anthropic.claude"
                        "GROK" -> "com.x.grok"
                        else -> ""
                    }

                    try {
                        val intent = if (packageName.isNotEmpty()) {
                            packageManager.getLaunchIntentForPackage(packageName)
                                ?: Intent(Intent.ACTION_VIEW, Uri.parse(modality.jumpUrl))
                        } else {
                            Intent(Intent.ACTION_VIEW, Uri.parse(modality.jumpUrl))
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(modality.jumpUrl))
                        startActivity(browserIntent)
                    }
                }
            }
            cardMainLayout.addView(btnOpen)

            cardContentLayout.addView(cardMainLayout)
            cardFrame.addView(cardContentLayout)

            // 卡片邊框植入 128 色流彩邊框 (RainbowBorderWrapper)
            val composeBorderView = ComposeView(this).apply {
                isClickable = false
                isFocusable = false
                setContent {
                    RainbowBorderWrapper(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 16.dp,
                        borderWidth = 2.2.dp
                    ) {}
                }
            }
            cardFrame.addView(composeBorderView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ))

            container.addView(cardFrame)
        }
    }
}
