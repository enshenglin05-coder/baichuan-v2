package com.example.myapplication01

import android.content.Context
import android.content.Intent

/**
 * AI 模態選擇與資源特色對話框管理器 (AI Modality Dialog Manager)
 * 轉為透過 Intent 啟動獨立 UI4 全螢幕頁面 (AiModalityActivity)，徹底廢除浮動彈窗
 */
object AiModalityDialogManager {

    data class AiModalityData(
        val id: String,
        val title: String,
        val icon: String,
        val subtitle: String,
        val contactPhone: String,
        val supportsDirectCall: Boolean,
        val jumpUrl: String
    )

    fun getModalities(langCode: String): List<AiModalityData> {
        return when (langCode) {
            "en" -> listOf(
                AiModalityData("ChatGPT", "ChatGPT Modality (OpenAI GPT-4o)", "🤖", "Advanced logic reasoning & multimodal chat", "0988-123-456", true, "https://chat.openai.com"),
                AiModalityData("GEMINI", "Gemini Modality (Google Gemini Nano)", "♊", "On-device multimodal & privacy computing", "0988-345-678", false, "https://gemini.google.com"),
                AiModalityData("CLAUDE", "Claude Modality (Anthropic Claude 3.5)", "🧠", "Deep semantic analysis & long-context reasoning", "0988-234-567", true, "https://claude.ai"),
                AiModalityData("GROK", "Grok Modality (xAI Grok-2)", "⚡", "Real-time web access & trend insights", "0988-456-789", true, "https://grok.x.ai")
            )
            "ja" -> listOf(
                AiModalityData("ChatGPT", "ChatGPT モダリティ (OpenAI GPT-4o)", "🤖", "高度論理推理と多模態対話", "0988-123-456", true, "https://chat.openai.com"),
                AiModalityData("GEMINI", "Gemini モダリティ (Google Gemini Nano)", "♊", "エッジ多模態とプライバシー安全演算", "0988-345-678", false, "https://gemini.google.com"),
                AiModalityData("CLAUDE", "Claude モダリティ (Anthropic Claude 3.5)", "🧠", "深層セマンティクス解析と長文推論", "0988-234-567", true, "https://claude.ai"),
                AiModalityData("GROK", "Grok モダリティ (xAI Grok-2)", "⚡", "リアルタイム情報検索とトレンド分析", "0988-456-789", true, "https://grok.x.ai")
            )
            "ko" -> listOf(
                AiModalityData("ChatGPT", "ChatGPT 모달리티 (OpenAI GPT-4o)", "🤖", "고급 논리 추론 및 다중 모달 대화", "0988-123-456", true, "https://chat.openai.com"),
                AiModalityData("GEMINI", "Gemini 모달리티 (Google Gemini Nano)", "♊", "온디바이스 멀티모달 및 개인정보 보호 연산", "0988-345-678", false, "https://gemini.google.com"),
                AiModalityData("CLAUDE", "Claude 모달리티 (Anthropic Claude 3.5)", "🧠", "심층 의미 분석 및 긴 문맥 추론", "0988-234-567", true, "https://claude.ai"),
                AiModalityData("GROK", "Grok 모달리티 (xAI Grok-2)", "⚡", "실시간 정보 검색 및 트렌드 인사이트", "0988-456-789", true, "https://grok.x.ai")
            )
            "vi" -> listOf(
                AiModalityData("ChatGPT", "Phương thức ChatGPT (OpenAI GPT-4o)", "🤖", "Suy luận logic cao cấp & đối thoại đa phương thức", "0988-123-456", true, "https://chat.openai.com"),
                AiModalityData("GEMINI", "Phương thức Gemini (Google Gemini Nano)", "♊", "Đa phương thức trên thiết bị & tính toán bảo mật", "0988-345-678", false, "https://gemini.google.com"),
                AiModalityData("CLAUDE", "Phương thức Claude (Anthropic Claude 3.5)", "🧠", "Phân tích ngữ nghĩa sâu & suy luận ngữ cảnh dài", "0988-234-567", true, "https://claude.ai"),
                AiModalityData("GROK", "Phương thức Grok (xAI Grok-2)", "⚡", "Kết nối thông tin thời gian thực & xu hướng", "0988-456-789", true, "https://grok.x.ai")
            )
            else -> listOf(
                AiModalityData("ChatGPT", "ChatGPT 模態 (OpenAI GPT-4o)", "🤖", "高階邏輯推理與多模態對話", "0988-123-456", true, "https://chat.openai.com"),
                AiModalityData("GEMINI", "Gemini 模態 (Google Gemini Nano)", "♊", "邊緣端多模態與隱私安全運算", "0988-345-678", false, "https://gemini.google.com"),
                AiModalityData("CLAUDE", "Claude 模態 (Anthropic Claude 3.5)", "🧠", "深度語意分析與長文本架構推理", "0988-234-567", true, "https://claude.ai"),
                AiModalityData("GROK", "Grok 模態 (xAI Grok-2)", "⚡", "即時資訊聯網與即時趨勢洞察", "0988-456-789", true, "https://grok.x.ai")
            )
        }
    }

    fun showAiModalitiesMenu(context: Context) {
        val intent = Intent(context, AiModalityActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
