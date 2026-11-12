package mobile_sandbox

import android.util.Log
import org.json.JSONObject
import java.io.File

/**
 * MobileSandboxLoader
 * 獨立隔離的沙盒參數讀取器範例。
 * 符合「選擇性讀取、安全容錯、一鍵切斷（刪除資料夾即恢復原狀）」規範。
 */
object MobileSandboxLoader {
    private const val TAG = "MobileSandboxLoader"
    private const val SANDBOX_DIR_NAME = "mobile_sandbox"
    private const val CONFIG_FILE_NAME = "sandbox_config.json"

    data class SandboxParams(
        val latitude: Double,
        val longitude: Double,
        val distanceRangeKm: Double,
        val countdownSeconds: Int,
        val ttsText: String
    )

    /**
     * 選擇性讀取沙盒設定檔。
     * 若資料夾或檔案不存在、或解析失敗，安全略過並回傳 null，絕對不報錯、不崩潰。
     */
    fun loadParameters(projectRootDir: File? = null): SandboxParams? {
        try {
            val root = projectRootDir ?: File(".")
            val sandboxDir = File(root, SANDBOX_DIR_NAME)
            if (!sandboxDir.exists() || !sandboxDir.isDirectory) {
                Log.d("SANDBOX_SOURCE", "⚠️ [本地預設] 未讀取到遠端沙盒，使用預設邏輯")
                return null
            }

            val configFile = File(sandboxDir, CONFIG_FILE_NAME)
            if (!configFile.exists() || !configFile.isFile) {
                Log.d("SANDBOX_SOURCE", "⚠️ [本地預設] 未讀取到遠端沙盒，使用預設邏輯")
                return null
            }

            val jsonString = configFile.readText(Charsets.UTF_8)
            if (jsonString.isBlank()) {
                Log.d("SANDBOX_SOURCE", "⚠️ [本地預設] 未讀取到遠端沙盒，使用預設邏輯")
                return null
            }

            val json = JSONObject(jsonString)
            val enabled = json.optBoolean("enabled", true)
            if (!enabled) {
                Log.d("SANDBOX_SOURCE", "⚠️ [本地預設] 未讀取到遠端沙盒，使用預設邏輯")
                return null
            }

            val paramsObj = json.optJSONObject("parameters") ?: run {
                Log.d("SANDBOX_SOURCE", "⚠️ [本地預設] 未讀取到遠端沙盒，使用預設邏輯")
                return null
            }
            val lat = paramsObj.optDouble("latitude", 25.0330)
            val lng = paramsObj.optDouble("longitude", 121.5654)
            val distance = paramsObj.optDouble("distance_range_km", 5.0)
            val countdown = paramsObj.optInt("countdown_seconds", 60)
            val ttsText = paramsObj.optString("tts_broadcast_text", "【百川現場沙盒】預設播報文本")

            Log.d("SANDBOX_SOURCE", "🔥 [沙盒熱更新] 成功載入遠端參數，播報文字: $ttsText")
            return SandboxParams(lat, lng, distance, countdown, ttsText)
        } catch (e: Exception) {
            Log.d("SANDBOX_SOURCE", "⚠️ [本地預設] 未讀取到遠端沙盒，使用預設邏輯")
            return null
        }
    }
}
