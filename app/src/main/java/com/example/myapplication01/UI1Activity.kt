package com.example.myapplication01

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.switchmaterial.SwitchMaterial
import android.content.Context
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import java.util.Locale

class UI1Activity : BaseActivity() {

    private var currentLanguageCode = "zh-TW"
    
    // 記錄當前選定的信仰卡片，預設為 null (未選擇)
    private var selectedReligionCard: ReligionCard? = null
    
    private lateinit var btnExit: TextView
    private lateinit var btnLocation: TextView
    private lateinit var btnAiModality: TextView
    private lateinit var btnLanguagePicker: TextView
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnBoxZh: TextView
    private lateinit var btnBoxEn: TextView
    private lateinit var btnBoxJa: TextView
    private lateinit var btnBoxKo: TextView
    private lateinit var btnBoxVi: TextView
    private lateinit var tvHint: TextView
    private lateinit var recyclerViewBlessings: RecyclerView
    private lateinit var btnEnterUI2: Button
    private lateinit var cbSelectionVerify: android.widget.CheckBox
    private lateinit var switchRainbow: SwitchMaterial

    private lateinit var topBar: View
    private lateinit var bottomBar: View

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchLocation()
        } else {
            Toast.makeText(this, getString(R.string.error_permission_location), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_ui1)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initViews()
        handleWindowInsets()
        
        setupRainbowEngine()
        
        // 刪除多餘的 checkConsent()，由 TermsPrivacyActivity 統一處理

        DeityBlessingTTS.init(this)

        // 禁止沙盒模擬，執行實際後端 TTS 檔案與封包排查並列印日誌
        BaichuanApiService.auditAndPullBackendTtsFiles(this) { success, count, url ->
            android.util.Log.d("BaichuanUI1", "🎯 [HTTP 成功 - 後端 TTS 排查完成] 成功=$success | 實際取得 JSON 數量=$count | 有效端點=$url")
        }

        setupListeners()
        setupLanguageSpinner()

        updateStaticUIText()
        loadCardsByLanguage(currentLanguageCode)
    }

    // 刪除 redundant consent 方法...

    private fun initViews() {
        btnExit = findViewById(R.id.btnExit)
        btnLocation = findViewById(R.id.btnLocation)
        btnAiModality = findViewById(R.id.btnAiModality)
        btnAiModality.bringToFront()
        btnLanguagePicker = findViewById(R.id.btnLanguagePicker)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        btnBoxZh = findViewById(R.id.btnBoxZh)
        btnBoxEn = findViewById(R.id.btnBoxEn)
        btnBoxJa = findViewById(R.id.btnBoxJa)
        btnBoxKo = findViewById(R.id.btnBoxKo)
        btnBoxVi = findViewById(R.id.btnBoxVi)
        
        setupToolboxListeners()

        tvHint = findViewById(R.id.tvHint)
        recyclerViewBlessings = findViewById(R.id.recyclerViewBlessings)
        btnEnterUI2 = findViewById(R.id.btnEnterUI2)
        cbSelectionVerify = findViewById(R.id.cbSelectionVerify)
        switchRainbow = findViewById(R.id.switchRainbow)
        
        btnEnterUI2.isEnabled = false
        btnEnterUI2.alpha = 0.5f
        
        switchRainbow.isChecked = GlobalState.isRainbowEnabled
        switchRainbow.setOnCheckedChangeListener { _, isChecked ->
            GlobalState.isRainbowEnabled = isChecked
        }
        
        topBar = findViewById(R.id.topBar)
        bottomBar = findViewById(R.id.bottomBar)

        recyclerViewBlessings.layoutManager = LinearLayoutManager(this)
        
        updateLocationButtonState()
    }

    private fun setupToolboxListeners() {
        btnBoxZh.setOnClickListener { switchLanguage("zh-TW") }
        btnBoxEn.setOnClickListener { switchLanguage("en") }
        btnBoxJa.setOnClickListener { switchLanguage("ja") }
        btnBoxKo.setOnClickListener { switchLanguage("ko") }
        btnBoxVi.setOnClickListener { switchLanguage("vi") }
    }

    private fun switchLanguage(langCode: String) {
        if (langCode != currentLanguageCode) {
            currentLanguageCode = langCode
            GlobalState.selectedLanguageCode = langCode
            val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(langCode)
            AppCompatDelegate.setApplicationLocales(appLocale)

            updateStaticUIText()
            updateToolboxSelection(langCode)
            updateTTSLanguage(langCode)
            loadCardsByLanguage(langCode)

            val pos = when (langCode) {
                "zh-TW" -> 0
                "en" -> 1
                "ja" -> 2
                "ko" -> 3
                "vi" -> 4
                else -> 0
            }
            spinnerLanguage.setSelection(pos, false)

            selectedReligionCard = null
            cbSelectionVerify.isChecked = false
            btnEnterUI2.isEnabled = false
            btnEnterUI2.alpha = 0.5f

            Toast.makeText(this, "已切換語言 / Language Switched: ${getLangDisplayName(langCode)}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToolboxSelection(activeLang: String) {
        val activeColor = Color.parseColor("#00FFC2")
        val inactiveColor = Color.WHITE

        val buttons = listOf(btnBoxZh, btnBoxEn, btnBoxJa, btnBoxKo, btnBoxVi)
        val codes = listOf("zh-TW", "en", "ja", "ko", "vi")

        for (i in codes.indices) {
            val isSelected = codes[i] == activeLang
            buttons[i].setTextColor(if (isSelected) activeColor else inactiveColor)
            buttons[i].setTypeface(null, if (isSelected) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            buttons[i].scaleX = if (isSelected) 1.05f else 1.0f
            buttons[i].scaleY = if (isSelected) 1.05f else 1.0f
        }
    }

    private fun getLangDisplayName(code: String): String {
        return when (code) {
            "zh-TW" -> "繁體中文"
            "en" -> "English"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "vi" -> "Tiếng Việt"
            else -> "繁體中文"
        }
    }

    private fun handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val topPadding = if (insets.top > 0) insets.top + (resources.displayMetrics.density * 8).toInt() else (resources.displayMetrics.density * 52).toInt()
            topBar.setPadding(topBar.paddingLeft, topPadding, topBar.paddingRight, topBar.paddingBottom)
            val bottomPadding = if (insets.bottom > 0) insets.bottom + (resources.displayMetrics.density * 16).toInt() else (resources.displayMetrics.density * 40).toInt()
            bottomBar.setPadding(bottomBar.paddingLeft, bottomBar.paddingTop, bottomBar.paddingRight, bottomPadding)
            windowInsets
        }
    }

    private fun setupListeners() {
        btnExit.setOnClickListener {
            finish()
        }

        btnExit.setOnLongClickListener {
            AdminReviewDialogManager.showAdminReviewDialog(this)
            true
        }

        btnLocation.setOnClickListener {
            checkPermissionAndFetchLocation()
        }

        btnAiModality.setOnClickListener {
            android.util.Log.d("AiButton", "啟動 AI 按鈕被點擊 (UI1 -> UI4)")
            Toast.makeText(this, "正在跳轉至 AI 模態全螢幕頁面 (UI4)...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AiModalityActivity::class.java)
            startActivity(intent)
        }

        btnLanguagePicker.setOnClickListener {
            spinnerLanguage.performClick()
        }

        btnEnterUI2.setOnClickListener {
            // 已選擇：觸發跳轉並傳遞資訊
            val cardName = selectedReligionCard?.let { getString(it.nameResId) } ?: "未選擇"
            val intent = Intent(this, UI2Activity::class.java).apply {
                putExtra("SELECTED_CARD_NAME", cardName)
                putExtra("LANGUAGE_CODE", currentLanguageCode)
            }
            startActivity(intent)
        }
    }

    private fun setupLanguageSpinner() {
        val languages = arrayOf("繁體中文", "English", "日本語", "한국어", "Tiếng Việt")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        spinnerLanguage.adapter = adapter

        val currentTags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val initialPosition = when {
            currentTags.contains("zh-TW") -> 0
            currentTags.contains("en") -> 1
            currentTags.contains("ja") -> 2
            currentTags.contains("ko") -> 3
            currentTags.contains("vi") -> 4
            else -> 0
        }
        spinnerLanguage.setSelection(initialPosition, false)
        currentLanguageCode = if (currentTags.isEmpty()) "zh-TW" else currentTags.split(",")[0]
        GlobalState.selectedLanguageCode = currentLanguageCode
        updateToolboxSelection(currentLanguageCode)

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newLangCode = when (position) {
                    0 -> "zh-TW"
                    1 -> "en"
                    2 -> "ja"
                    3 -> "ko"
                    4 -> "vi"
                    else -> "zh-TW"
                }
                
                if (newLangCode != currentLanguageCode) {
                    switchLanguage(newLangCode)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun checkPermissionAndFetchLocation() {
        val fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun fetchLocation() {
        btnLocation.isEnabled = false
        btnLocation.text = "定位中..."

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    btnLocation.isEnabled = true
                    if (location != null) {
                        GlobalState.globalLatitude = location.latitude
                        GlobalState.globalLongitude = location.longitude
                        GlobalState.isLocated = true
                        updateLocationButtonState()
                        Toast.makeText(this, getString(R.string.msg_location_success), Toast.LENGTH_SHORT).show()
                    } else {
                        btnLocation.text = "📍 我的位置"
                        Toast.makeText(this, getString(R.string.error_location_failed), Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    btnLocation.isEnabled = true
                    btnLocation.text = "📍 我的位置"
                    Toast.makeText(this, getString(R.string.error_location_error, it.message), Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            btnLocation.isEnabled = true
            btnLocation.text = "📍 我的位置"
        }
    }

    private fun updateLocationButtonState() {
        if (GlobalState.isLocated) {
            btnLocation.text = "📍 已定位"
            btnLocation.setTextColor(Color.parseColor("#00FFC2")) // greenAccent style
        } else {
            btnLocation.text = "📍 我的位置"
            btnLocation.setTextColor(Color.WHITE)
        }
    }

    private fun setupRainbowEngine() {
        val rootLayout = findViewById<FrameLayout>(R.id.rootLayout)
        val composeView = ComposeView(this).apply {
            // 確保不攔截點擊事件
            isClickable = false
            isFocusable = false
            setContent {
                Box(modifier = Modifier.fillMaxSize()) {
                    LightAppFullscreenAtmosphere() // 1. 全螢幕旋轉呼吸背景
                    LightAppSuiteBox()             // 2. 外層邊框流光
                    ReligionFlashOverlay()         // 3. 閃爆層
                }
            }
        }
        rootLayout.addView(composeView, 0, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
    }

    private fun updateStaticUIText() {
        tvHint.text = getString(R.string.ui1_hint)
        btnEnterUI2.text = getString(R.string.btn_enter_ui2)
        switchRainbow.text = getString(R.string.label_rainbow_switch)
        btnExit.text = getString(R.string.exit)

        val langName = when (currentLanguageCode) {
            "zh-TW" -> "繁體中文"
            "en" -> "English"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "vi" -> "Tiếng Việt"
            else -> "繁體中文"
        }
        btnLanguagePicker.text = langName
    }

    private fun loadCardsByLanguage(langCode: String) {
        val cards = ReligionRepository.getCardsByLanguage(langCode)
        recyclerViewBlessings.adapter = ReligionAdapter(cards) { card ->
            // 記錄選定狀態
            selectedReligionCard = card
            
            // 新增：解鎖驗證與勾選框
            cbSelectionVerify.isChecked = true
            btnEnterUI2.isEnabled = true
            btnEnterUI2.alpha = 1.0f

            // 使用當前語言播放 TTS 並存入全域變數
            val descText = getString(card.descResId)
            GlobalState.selectedBlessingText = descText
            GlobalState.selectedDeityId = card.id
            
            // 更新全域主題色彩
            val themeColors = ThemeUtils.getColorsForCategory(card.category)
            GlobalState.selectedThemeColorHex = themeColors.first
            GlobalState.selectedThemeAccentColorHex = themeColors.second
            GlobalState.selectedThemeTextColorHex = themeColors.third
            GlobalState.flashColor = androidx.compose.ui.graphics.Color(ThemeUtils.parseColor(themeColors.first))
            
            playTTS(descText)
            
            Toast.makeText(this, getString(card.nameResId), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTTSLanguage(langCode: String) {
        DeityBlessingTTS.updateLanguage()
    }

    private fun playTTS(text: String) {
        DeityBlessingTTS.speakBlessingAndSequenceBackendBroadcasts(this, text)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
