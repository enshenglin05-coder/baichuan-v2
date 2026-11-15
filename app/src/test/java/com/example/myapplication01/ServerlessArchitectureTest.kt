package com.example.myapplication01

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ServerlessArchitectureTest {

    @Test
    fun testRepositoryAndTtsPacketLogic() {
        // 1. 驗證資料夾初始化邏輯
        val tempDir = File(System.getProperty("java.io.tmpdir"), "baichuan_test_root")
        ServerlessRepositoryInitializer.initializeRepositories(tempDir)

        val ttsDir = File(tempDir, ServerlessRepositoryInitializer.TTS_REPOSITORY_FOLDER)
        val auditDir = File(tempDir, ServerlessRepositoryInitializer.AUDIT_BLOCKLIST_REPO)

        assertTrue("TTS 倉庫資料夾必須成功建立", ttsDir.exists())
        assertTrue("黑名單審核隔離區必須成功建立", auditDir.exists())

        // 2. 驗證 100 次倒數與時間戳憑證封包
        val packet = TtsAuditPacket(cardId = "CARD_001", senderId = "SENDER_XYZ", remainingQuota = 2)

        // 第一次觸動
        val success1 = packet.triggerTouch(25.033, 121.565, "測試語音內容一")
        assertTrue("剩餘額度大於0應成功觸動", success1)
        assertEquals("剩餘次數應為 1", 1, packet.remainingQuota)

        // 第二次觸動（用罄）
        val success2 = packet.triggerTouch(25.034, 121.566, "測試語音內容二")
        assertTrue("最後一次額度應成功觸動並結算", success2)
        assertEquals("剩餘次數應為 0", 0, packet.remainingQuota)
        assertTrue("額度用罄應標記為 completed", packet.isCompleted)

        // 3. 驗證黑名單自動攔截防線
        TtsAndSecurityGuard.updateBlocklist(listOf("BAD_USER_999"))
        assertTrue("命中黑名單應被強制阻斷", TtsAndSecurityGuard.isBlocked("BAD_USER_999"))
        assertFalse("正常用戶不應被阻斷", TtsAndSecurityGuard.isBlocked("GOOD_USER_001"))
    }
}
