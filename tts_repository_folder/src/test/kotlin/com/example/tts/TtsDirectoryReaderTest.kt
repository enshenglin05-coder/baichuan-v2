package com.example.tts

import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files

class TtsDirectoryReaderTest {

    private lateinit var tempRoot: File
    private lateinit var resolver: TtsRootResolver
    private lateinit var reader: TtsDirectoryReader

    @Before
    fun setUp() {
        tempRoot = Files.createTempDirectory("tts_test_root").toFile()
        resolver = TtsRootResolver(tempRoot)
        reader = TtsDirectoryReader(resolver)
    }

    @After
    fun tearDown() {
        tempRoot.deleteRecursively()
    }

    @Test
    fun testRootResolution() {
        val root = resolver.resolveRoot()
        assertTrue(root.exists())
        assertEquals(tempRoot.canonicalFile, root)
    }

    @Test
    fun testMissingDirectoryDiscovery() {
        assertThrows(FileNotFoundException::class.java) {
            reader.discoverJsonFiles("non_existent_subdir")
        }
    }

    @Test
    fun testJsonFileDiscoveryAndReading() {
        val childDir = File(tempRoot, "sub_packets")
        childDir.mkdirs()

        val sampleJsonFile = File(childDir, "ui5_manual_packet_001.json")
        val jsonContent = """
            {
              "packetId": "ui5_pkt_1234567_01",
              "packetCode": "1234567",
              "text": "測試語音封包內容",
              "remainingCount": 50,
              "timestamp": "2026-03-31 12:00:00.000",
              "latitude": 25.033,
              "longitude": 121.565,
              "radiusKm": 6.0,
              "isArchived": false
            }
        """.trimIndent()
        sampleJsonFile.writeText(jsonContent)

        val discoveredFiles = reader.discoverJsonFiles("sub_packets")
        assertEquals(1, discoveredFiles.size)
        assertEquals("ui5_manual_packet_001.json", discoveredFiles[0].name)

        val manualPackets = reader.discoverManualPackets("sub_packets")
        assertEquals(1, manualPackets.size)

        val packet = reader.readPacketFile("sub_packets/ui5_manual_packet_001.json")
        assertEquals("ui5_pkt_1234567_01", packet.packetId)
        assertEquals("測試語音封包內容", packet.text)
        assertEquals(50, packet.remainingCount)
    }

    @Test
    fun testInvalidPathsAndTraversalRejection() {
        assertThrows(SecurityException::class.java) {
            resolver.resolveChildPath("../outside_file.json")
        }

        assertThrows(SecurityException::class.java) {
            resolver.resolveChildPath("sub/../../escape.json")
        }

        assertThrows(SecurityException::class.java) {
            resolver.resolveChildPath("/etc/passwd")
        }

        assertThrows(SecurityException::class.java) {
            resolver.resolveChildPath("C:\\Windows\\system.ini")
        }
    }
}
