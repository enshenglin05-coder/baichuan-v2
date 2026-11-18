package com.example.myapplication01

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class UI5PasswordActivity : BaseActivity() {

    private lateinit var btnPasswordBack: TextView
    private lateinit var etTop10: TextInputEditText
    private lateinit var etBottom10: TextInputEditText
    private lateinit var btnVerifyAndEnterUI5: MaterialButton
    private lateinit var tvPasswordHint: TextView

    // 內建原生寫死之安全密碼 (上方 10 碼數字 0-9，下方 10 碼英文字母 A-J)
    private val expectedTop10 = "1234567890"
    private val expectedBottom10 = "ABCDEFGHIJ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ui5_password)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnPasswordBack = findViewById(R.id.btnPasswordBack)
        etTop10 = findViewById(R.id.etTop10)
        etBottom10 = findViewById(R.id.etBottom10)
        btnVerifyAndEnterUI5 = findViewById(R.id.btnVerifyAndEnterUI5)
        tvPasswordHint = findViewById(R.id.tvPasswordHint)
    }

    private fun setupListeners() {
        btnPasswordBack.setOnClickListener {
            finish()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 自動轉大寫
            }
        }

        etTop10.addTextChangedListener(textWatcher)
        etBottom10.addTextChangedListener(textWatcher)

        btnVerifyAndEnterUI5.setOnClickListener {
            verifyAndProceed()
        }
    }

    private fun verifyAndProceed() {
        val topText = etTop10.text?.toString()?.trim()?.uppercase() ?: ""
        val bottomText = etBottom10.text?.toString()?.trim()?.uppercase() ?: ""

        // 檢查長度
        if (topText.length != 10 || bottomText.length != 10) {
            Toast.makeText(this, "驗證失敗：上方必須滿 10 碼，下方必須滿 10 碼（總計 20 碼）", Toast.LENGTH_SHORT).show()
            tvPasswordHint.text = "❌ 驗證失敗：長度不足 20 碼（上方 ${topText.length}/10, 下方 ${bottomText.length}/10）"
            tvPasswordHint.setTextColor(android.graphics.Color.parseColor("#FF4444"))
            return
        }

        // 檢查字元是否符合規則 (0-9 與 A-J)
        val regex = Regex("^[0-9A-J]{10}$")
        if (!regex.matches(topText)) {
            Toast.makeText(this, "驗證失敗：上方 10 碼僅允許數字 0-9 與英文字母 A-J", Toast.LENGTH_SHORT).show()
            tvPasswordHint.text = "❌ 上方 10 碼包含不合法字元（僅限 0-9, A-J）"
            tvPasswordHint.setTextColor(android.graphics.Color.parseColor("#FF4444"))
            return
        }

        if (!regex.matches(bottomText)) {
            Toast.makeText(this, "驗證失敗：下方 10 碼僅允許數字 0-9 與英文字母 A-J", Toast.LENGTH_SHORT).show()
            tvPasswordHint.text = "❌ 下方 10 碼包含不合法字元（僅限 0-9, A-J）"
            tvPasswordHint.setTextColor(android.graphics.Color.parseColor("#FF4444"))
            return
        }

        // 檢查是否與內建原生寫死密碼相符
        if (topText == expectedTop10 && bottomText == expectedBottom10) {
            Toast.makeText(this, "✅ 驗證成功！正在進入 UI 5 核心配置...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, UI5Activity::class.java)
            startActivity(intent)
            finish() // 銷毀當前密碼頁，確保離開後需重新驗證
        } else {
            Toast.makeText(this, "❌ 密碼錯誤，請重新輸入 20 碼安全組合", Toast.LENGTH_SHORT).show()
            tvPasswordHint.text = "❌ 20 碼安全驗證密碼錯誤，請重新核對"
            tvPasswordHint.setTextColor(android.graphics.Color.parseColor("#FF4444"))
        }
    }
}
