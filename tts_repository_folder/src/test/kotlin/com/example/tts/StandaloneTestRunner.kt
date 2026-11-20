package com.example.tts

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files

fun main() {
    println("=== Running Standalone TTS Directory Reader Verification ===")

    val tempRoot = Files.createTempDirectory("tts_standalone_root").toFile()
    try {
        val resolver = TtsRootResolver(tempRoot)
        val reader = TtsDirectoryReader(resolver)

        // 1. Test root resolution
        val root = resolver.resolveRoot()
        assert(root.exists()) { "Root should exist" }
        println("[PASS] Root resolution test passed: ${root.absolutePath}")

        // 2. Test missing directory
        try {
            reader.discoverJsonFiles("missing_sub")
            throw AssertionError("Expected FileNotFoundException for missing directory")
        } catch (e: FileNotFoundException) {
            println("[PASS] Missing directory test passed: ${e.message}")
        }

        // 3. Test child-directory reading & JSON file discovery
        val subDir = File(tempRoot, "packets")
        subDir.mkdirs()
        val jsonFile = File(subDir, "ui5_manual_packet_999.json")
        jsonFile.writeText("""
            {
              "packetId": "ui5_pkt_9999999_01",
              "packetCode": "9999999",
              "text": "獨立 TTS 讀取測試內容",
              "remainingCount": 100,
              "timestamp": "2026-03-31 15:30:00.000",
              "latitude": 25.0,
              "longitude": 121.0,
              "radiusKm": 6.0,
              "isArchived": false
            }
        """.trimIndent())

        val discovered = reader.discoverJsonFiles("packets")
        assert(discovered.size == 1) { "Expected 1 json file" }
        assert(discovered[0].name == "ui5_manual_packet_999.json")
        println("[PASS] JSON file discovery test passed")

        val manualPackets = reader.discoverManualPackets("packets")
        assert(manualPackets.size == 1) { "Expected 1 manual packet" }
        println("[PASS] Manual packet discovery test passed")

        val packet = reader.readPacketFile("packets/ui5_manual_packet_999.json")
        assert(packet.packetId == "ui5_pkt_9999999_01")
        assert(packet.text == "獨立 TTS 讀取測試內容")
        println("[PASS] Packet file reading test passed: ${packet.text}")

        // 4. Test invalid paths / traversal rejection
        val invalidPaths = listOf(
            "../outside.json",
            "packets/../../secret.json",
            "/etc/passwd",
            "C:\\Windows\\win.ini"
        )
        for (path in invalidPaths) {
            try {
                resolver.resolveChildPath(path)
                throw AssertionError("Expected SecurityException for path: $path")
            } catch (e: SecurityException) {
                println("[PASS] Invalid path correctly rejected: $path -> ${e.message}")
            }
        }

        println("=== ALL TTS DIRECTORY READER TESTS PASSED SUCCESSFULLY ===")
    } finally {
        tempRoot.deleteRecursively()
    }
}
