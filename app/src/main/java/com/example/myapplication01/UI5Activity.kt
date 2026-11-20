package com.example.myapplication01

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UI5Activity : BaseActivity() {

    private lateinit var btnUI5Back: TextView
    private lateinit var seekBarQuantity: SeekBar
    private lateinit var tvQuantityValue: TextView
    private lateinit var switchManualOverride: SwitchMaterial
    private lateinit var etPacketCode: TextInputEditText
    private lateinit var etLatitude: TextInputEditText
    private lateinit var etLongitude: TextInputEditText
    private lateinit var etTtsMessage: TextInputEditText
    private lateinit var btnConfirmSubmit: MaterialButton

    private var broadcastQuantity = 100
    private var deliveryDistanceKm = 6

    // 防重複觸發保護標記
    private var isSubmitting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui5)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnUI5Back = findViewById(R.id.btnUI5Back)
        seekBarQuantity = findViewById(R.id.seekBarQuantity)
        tvQuantityValue = findViewById(R.id.tvQuantityValue)
        switchManualOverride = findViewById(R.id.switchManualOverride)
        etPacketCode = findViewById(R.id.etPacketCode)
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        etTtsMessage = findViewById(R.id.etTtsMessage)
        btnConfirmSubmit = findViewById(R.id.btnConfirmSubmit)

        // 預設啟用手動覆蓋並使經緯度完全顯示且可手動修改
        switchManualOverride.isChecked = true
        etLatitude.isEnabled = true
        etLongitude.isEnabled = true

        // Initialize progress
        seekBarQuantity.progress = 99 // 99 + 1 = 100
        tvQuantityValue.text = "目前數量: 100"

        // Wire GPS Auto Populate button if present
        findViewById<MaterialButton>(R.id.btnGetGps)?.setOnClickListener {
            val lat = GlobalState.globalLatitude ?: LocationFlowManager.currentLocation?.first?.toDouble()
            val lon = GlobalState.globalLongitude ?: LocationFlowManager.currentLocation?.second?.toDouble()
            if (lat != null && lon != null) {
                etLatitude.setText(lat.toString())
                etLongitude.setText(lon.toString())
                Toast.makeText(this, "已帶入當前 GPS: $lat, $lon", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "⚠️ 無法取得當前 GPS 座標，請手動輸入", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        btnUI5Back.setOnClickListener {
            finish()
        }

        seekBarQuantity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                broadcastQuantity = progress + 1
                tvQuantityValue.text = "目前數量: $broadcastQuantity"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        switchManualOverride.setOnCheckedChangeListener { _, isChecked ->
            etLatitude.isEnabled = true
            etLongitude.isEnabled = true
            if (isChecked) {
                val lat = GlobalState.globalLatitude ?: LocationFlowManager.currentLocation?.first?.toDouble()
                val lon = GlobalState.globalLongitude ?: LocationFlowManager.currentLocation?.second?.toDouble()
                if (lat != null && lon != null) {
                    etLatitude.setText(lat.toString())
                    etLongitude.setText(lon.toString())
                    Toast.makeText(this, "已獲取當前位置: $lat, $lon", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnConfirmSubmit.setOnClickListener {
            if (isSubmitting) {
                android.util.Log.w("UI5Activity", "⚠️ 正在處理送出中，忽略重複點擊")
                return@setOnClickListener
            }
            isSubmitting = true
            btnConfirmSubmit.isEnabled = false

            releaseBroadcastAndSync()
        }
    }

    private fun releaseBroadcastAndSync() {
        val ttsMessage = etTtsMessage.text?.toString()?.trim() ?: ""
        val packetCode = etPacketCode.text?.toString()?.trim()?.ifBlank { "1234567" } ?: "1234567"
        val packetId = "ui5_pkt_${packetCode}_${System.currentTimeMillis()}"
        val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

        if (ttsMessage.isEmpty()) {
            Toast.makeText(this, "請輸入播報訊息 (TTS)", Toast.LENGTH_SHORT).show()
            isSubmitting = false
            btnConfirmSubmit.isEnabled = true
            return
        }

        // 讀取真實座標，絕不產生缺漏值或預設虛構座標
        val lat = if (switchManualOverride.isChecked) {
            etLatitude.text?.toString()?.toDoubleOrNull()
        } else {
            GlobalState.globalLatitude ?: LocationFlowManager.currentLocation?.first?.toDouble()
        }
        val lon = if (switchManualOverride.isChecked) {
            etLongitude.text?.toString()?.toDoubleOrNull()
        } else {
            GlobalState.globalLongitude ?: LocationFlowManager.currentLocation?.second?.toDouble()
        }

        if (lat == null || lon == null || lat.isNaN() || lon.isNaN()) {
            Toast.makeText(this, "❌ 無法取得有效的經緯度座標，請輸入或選擇定位", Toast.LENGTH_SHORT).show()
            isSubmitting = false
            btnConfirmSubmit.isEnabled = true
            return
        }

        val distanceInput = findViewById<TextInputEditText>(R.id.etDistanceRange)?.text?.toString()?.toDoubleOrNull()
        if (distanceInput != null && distanceInput > 0.0) {
            deliveryDistanceKm = distanceInput.toInt()
        }
        val radiusKm = if (distanceInput != null && distanceInput > 0.0) distanceInput else deliveryDistanceKm.toDouble()

        val geofenceParams = mapOf<String, Any>(
            "centerLat" to lat,
            "centerLng" to lon,
            "radiusKm" to radiusKm
        )

        val auditPacket = TtsAuditPacket(
            cardId = packetId,
            senderId = "UI5_Manual_User_Submit",
            totalQuota = broadcastQuantity,
            remainingQuota = broadcastQuantity
        )

        try {
            ReligionRepository.saveTtsPacket(auditPacket)
            
            val savedFile = TtsRepositoryManager.saveTtsPacket(
                packetId = packetId,
                text = ttsMessage,
                remainingCount = broadcastQuantity,
                context = this,
                latitude = lat,
                longitude = lon,
                radiusKm = radiusKm,
                timestamp = timestampStr,
                packetCode = packetCode,
                formulaIdentifier = DistanceUtils.FORMULA_ID,
                formulaVersion = DistanceUtils.FORMULA_VERSION,
                geofenceType = "RADIAL_CIRCLE",
                geofenceParams = geofenceParams
            )

            if (savedFile != null) {
                Toast.makeText(this, "✅ UI 5 手動 TTS 封包已成功送出並同步！", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "❌ 寫入失敗", Toast.LENGTH_SHORT).show()
                isSubmitting = false
                btnConfirmSubmit.isEnabled = true
            }
        } catch (e: Exception) {
            Toast.makeText(this, "❌ 錯誤: ${e.message}", Toast.LENGTH_SHORT).show()
            isSubmitting = false
            btnConfirmSubmit.isEnabled = true
        }
    }
}
