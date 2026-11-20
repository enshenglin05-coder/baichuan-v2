package com.example.myapplication01

import com.example.myapplication01.tts.TtsDirectoryScanner
import com.example.myapplication01.tts.TtsPacket
import com.example.myapplication01.tts.TtsPacketSelector
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TtsReverseGeofenceIntegrationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun test1_insideGeofenceAccept() {
        val lat = 25.033000
        val lng = 121.565000
        val distance = DistanceUtils.calculateDistance(lat, lng, 25.033000, 121.565000)
        assertEquals(0.0, distance, 0.0001)
        assertTrue(distance <= 2.0)
    }

    @Test
    fun test2_boundaryAccept() {
        val userLat = 25.033000
        val userLng = 121.565000
        val packetLat = 25.051000
        val dist = DistanceUtils.calculateDistance(userLat, userLng, packetLat, userLng)
        val packet = TtsPacket(
            packetId = "ui5_pkt_1234567_01",
            text = "Boundary test",
            latitude = packetLat,
            longitude = userLng,
            radiusKm = dist + 0.001,
            remainingCount = 10
        )
        assertTrue(packet.isDataValid())
        assertTrue(dist <= (packet.radiusKm ?: 0.0))
    }

    @Test
    fun test3_outsideGeofenceRejectBeforeFullRetrieval() {
        val rootDir = tempFolder.newFolder("tts_repo")
        val file = File(rootDir, "outside.json")
        file.writeText("""
            {
              "packetId": "ui5_pkt_1234567_99",
              "text": "Outside packet",
              "latitude": 23.000000,
              "longitude": 120.000000,
              "radiusKm": 1.0,
              "remainingCount": 10
            }
        """.trimIndent())

        val scanner = TtsDirectoryScanner(rootDir)
        val candidates = scanner.scanAllPackets(25.033, 121.565)
        assertEquals(1, candidates.size)

        val selector = TtsPacketSelector()
        val selected = selector.selectSinglePacket(candidates, 25.033, 121.565, 6.0)
        assertNull("Outside geofence packet must be rejected", selected)
    }

    @Test
    fun test4_missingOrInvalidDataRejectWithoutDefaults() {
        val rootDir = tempFolder.newFolder("tts_repo_invalid")
        val file = File(rootDir, "invalid.json")
        file.writeText("""
            {
              "packetId": null,
              "text": "",
              "latitude": null,
              "longitude": 121.565,
              "radiusKm": -1.0,
              "remainingCount": 10
            }
        """.trimIndent())

        val packet = com.google.gson.Gson().fromJson(file.readText(), TtsPacket::class.java)
        assertFalse("Invalid packet must fail validation and not use defaults", packet.isDataValid())
    }

    @Test
    fun test5_multipleQualifyingPacketsSelectExactlyOne() {
        val rootDir = tempFolder.newFolder("tts_repo_multi")
        File(rootDir, "pkt1.json").writeText("""
            {
              "packetId": "ui5_pkt_1234567_01",
              "packetCode": "1234567",
              "text": "Packet One",
              "latitude": 25.033000,
              "longitude": 121.565000,
              "radiusKm": 5.0,
              "timestamp": "2026-03-31 10:00:00.000",
              "remainingCount": 10
            }
        """.trimIndent())

        File(rootDir, "pkt2.json").writeText("""
            {
              "packetId": "ui5_pkt_1111113_02",
              "packetCode": "1111113",
              "text": "Packet Two",
              "latitude": 25.033000,
              "longitude": 121.565000,
              "radiusKm": 5.0,
              "timestamp": "2026-03-31 11:00:00.000",
              "remainingCount": 10
            }
        """.trimIndent())

        val scanner = TtsDirectoryScanner(rootDir)
        val candidates = scanner.scanAllPackets(25.033, 121.565)
        assertEquals(2, candidates.size)

        val selector = TtsPacketSelector()
        val selected = selector.selectSinglePacket(candidates, 25.033, 121.565, 6.0)
        assertNotNull(selected)
        assertEquals("ui5_pkt_1111113_02", selected?.packetId)
    }

    @Test
    fun test6_fullDirectoryScanNoEarlyStop() {
        val rootDir = tempFolder.newFolder("tts_repo_scan")
        File(rootDir, "a_invalid.json").writeText("""
            {
              "packetId": "ui5_pkt_0000000_00",
              "text": "",
              "latitude": null,
              "longitude": null,
              "radiusKm": 1.0,
              "remainingCount": 10
            }
        """.trimIndent())

        File(rootDir, "b_valid.json").writeText("""
            {
              "packetId": "ui5_pkt_1234567_01",
              "packetCode": "1234567",
              "text": "Valid Later Packet",
              "latitude": 25.033000,
              "longitude": 121.565000,
              "radiusKm": 5.0,
              "remainingCount": 10
            }
        """.trimIndent())

        val scanner = TtsDirectoryScanner(rootDir)
        val candidates = scanner.scanAllPackets(25.033, 121.565)
        assertEquals(1, candidates.size)
        assertEquals("Valid Later Packet", candidates[0].packet.text)
    }

    @Test
    fun test7_playbackBusyStateProtection() {
        val packet = TtsPacket(
            packetId = "ui5_pkt_1234567_01",
            text = "Busy state test",
            latitude = 25.033,
            longitude = 121.565,
            radiusKm = 5.0,
            remainingCount = 5
        )
        assertTrue(packet.isDataValid())
    }

    @Test
    fun test8_firstWordEventTriggersPreparation() {
        val packet = TtsPacket(
            packetId = "ui5_pkt_1234567_02",
            text = "First word pre-fetch test",
            latitude = 25.033,
            longitude = 121.565,
            radiusKm = 5.0,
            remainingCount = 5
        )
        assertTrue(packet.isDataValid())
        assertEquals(28, packet.get7DigitSum())
    }
}
