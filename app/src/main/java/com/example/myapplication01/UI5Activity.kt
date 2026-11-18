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
    private lateinit var etLatitude: TextInputEditText
    private lateinit var etLongitude: TextInputEditText
    // Removed seekBarDistance and tvDistanceValue
    // private lateinit var seekBarDistance: SeekBar
    // private lateinit var tvDistanceValue: TextView
    private lateinit var etTtsMessage: TextInputEditText
    private lateinit var btnConfirmSettings: MaterialButton
    private lateinit var btnTriggerManually: MaterialButton
    private lateinit var tvConnectionStatus: TextView
    private lateinit var btnReleaseBroadcast: MaterialButton

    private var broadcastQuantity = 100
    private var deliveryDistanceKm = 6

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
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
        // Removed seekBarDistance and tvDistanceValue as they do not exist in the layout
        etTtsMessage = findViewById(R.id.etTtsMessage)
        btnConfirmSettings = findViewById(R.id.btnConfirmSettings)
        btnTriggerManually = findViewById(R.id.btnTriggerManually)
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)
        btnReleaseBroadcast = findViewById(R.id.btnReleaseBroadcast)

        // Initialize progress
        seekBarQuantity.progress = 99 // 99 + 1 = 100
        tvQuantityValue.text = "Current Value: 100"

        // seekBarDistance.progress = 5 // 5 + 1 = 6 km
        // tvDistanceValue.text = "Current Value: 6 km"
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
            etLatitude.isEnabled = isChecked
            etLongitude.isEnabled = isChecked
            val stateText = if (isChecked) "手動覆蓋模式 (自訂 GPS)" else "預設 GPS 模式"
            tvConnectionStatus.text = "連線狀態：$stateText | 已連線並同步"
            Toast.makeText(this, stateText, Toast.LENGTH_SHORT).show()
        }

        // Removed listeners that used non-existent views
        /*
        seekBarDistance.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                deliveryDistanceKm = progress + 1
                tvDistanceValue.text = "目前數值: $deliveryDistanceKm km"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        */

        btnConfirmSettings.setOnClickListener {
            val lat = etLatitude.text?.toString()?.trim()?.toDoubleOrNull() ?: 25.0330
            val lon = etLongitude.text?.toString()?.trim()?.toDoubleOrNull() ?: 121.5654

            if (switchManualOverride.isChecked) {
                GlobalState.globalLatitude = lat
                GlobalState.globalLongitude = lon
                GlobalState.isLocated = true
            }

            tvConnectionStatus.text = "連線狀態：設定已儲存 (數量: $broadcastQuantity, 距離: ${deliveryDistanceKm}km)"
            Toast.makeText(this, "✅ 廣播與外送設定已成功儲存！", Toast.LENGTH_SHORT).show()
        }

        btnTriggerManually.setOnClickListener {
            val message = etTtsMessage.text?.toString()?.trim() ?: "百川神明祝福：手動廣播與外送觸發"
            DeityBlessingTTS.speakCustomOrCurrent(this, message)
            tvConnectionStatus.text = "連線狀態：已手動觸發語音廣播"
            Toast.makeText(this, "🔊 手動觸發語音廣播成功！", Toast.LENGTH_SHORT).show()
        }

        btnReleaseBroadcast.setOnClickListener {
            releaseBroadcastAndSync()
        }
    }

    private fun releaseBroadcastAndSync() {
        val ttsMessage = etTtsMessage.text?.toString()?.trim() ?: "百川神明祝福：在地安心外送與即時定位服務已啟動"
        val packetId = "broadcast_delivery_" + System.currentTimeMillis()
        val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        android.util.Log.d("UI5Activity", "開始打包 TTS 封包: packetId=$packetId, 數量=$broadcastQuantity, 時間=$timestampStr")

        if (ttsMessage.isEmpty()) {
            android.util.Log.e("UI5Activity", "TTS 訊息內容為空")
            Toast.makeText(this, "請輸入播報訊息 (TTS)", Toast.LENGTH_SHORT).show()
            return
        }

        val lat = if (switchManualOverride.isChecked) (etLatitude.text?.toString()?.toDoubleOrNull() ?: 25.0330) else (GlobalState.globalLatitude ?: 25.0330)
        val lon = if (switchManualOverride.isChecked) (etLongitude.text?.toString()?.toDoubleOrNull() ?: 121.5654) else (GlobalState.globalLongitude ?: 121.5654)

        // 1. Create audit packet with full broadcast quantity quota (without premature triggerTouch decrement)
        val auditPacket = TtsAuditPacket(
            cardId = packetId,
            senderId = "Broadcast_Delivery_Setup",
            totalQuota = broadcastQuantity,
            remainingQuota = broadcastQuantity
        )

        try {
            android.util.Log.d("UI5Activity", "TTS 封包建立完成: 剩餘配額=${auditPacket.remainingQuota}, 時間=$timestampStr")
            
            // 2. Save and sync to local and remote repository via TtsRepositoryManager and ReligionRepository
            ReligionRepository.saveTtsPacket(auditPacket)
            
            val savedFile = TtsRepositoryManager.saveTtsPacket(
                packetId = packetId,
                text = ttsMessage,
                remainingCount = broadcastQuantity,
                context = this,
                latitude = lat,
                longitude = lon,
                radiusKm = deliveryDistanceKm.toDouble(),
                timestamp = timestampStr
            )

            if (savedFile != null) {
                android.util.Log.i("UI5Activity", "✅ 成功釋出廣播並同步至倉庫: ${savedFile.name} (數量: $broadcastQuantity, 緯度: $lat, 經度: $lon)")
                Toast.makeText(this, "✅ 廣播與外送已成功釋出！已同步至倉庫。", Toast.LENGTH_LONG).show()
                finish()
            } else {
                android.util.Log.e("UI5Activity", "❌ 釋出廣播失敗: 保存檔案為空")
                Toast.makeText(this, "❌ 廣播釋出與同步失敗", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("UI5Activity", "❌ 處理 TTS 封包發生異常", e)
            Toast.makeText(this, "❌ 錯誤: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
