# Mobile Sandbox (mobile_sandbox/)

此資料夾為「百川」專案的手機端現場動態參數與 TTS 內容傳遞獨立隔離沙盒。

## 🎯 設計目標與規範

1. **絕對解耦與禁區保護**：
   - 本資料夾與主程式核心邏輯完全隔離。
   - **絕對禁止**修改任何現有的主程式檔案（如 `MainActivity.kt`、`UI1Activity.kt` 等），維持原樣不動。

2. **動態參數內容 (`sandbox_config.json`)**：
   - 支援現場動態經緯度 (`latitude`, `longitude`)。
   - 支援搜尋距離範圍 (`distance_range_km`)。
   - 支援倒數計時設定 (`countdown_seconds`)。
   - 支援自訂 TTS 語音播報文本 (`tts_broadcast_text`)。

3. **安全容錯與一鍵切斷設計**：
   - 具備安全防護機制：若讀取端檢測到 `mobile_sandbox/` 存在且內容有效，則載入參數執行；
   - 若該資料夾被刪除、為空或不存在，系統將自動略過，**絕對不報錯、不崩潰 (Zero Crash)**。
   - **一鍵恢復**：未來若不需要此沙盒，只需將整個 `mobile_sandbox/` 資料夾刪除或抹除，系統即可 100% 恢復原狀，不留任何後遺症。
