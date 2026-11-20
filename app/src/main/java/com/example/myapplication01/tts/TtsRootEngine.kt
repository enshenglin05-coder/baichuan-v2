package com.example.myapplication01.tts

import android.content.Context
import java.io.File
import java.util.concurrent.Executors

/**
 * Main coordinator in the NEW TTS root directory.
 *
 * Enforces STRICT DATA VALIDATION:
 * 1. Missing or unreadable GPS/distance comparison data = NULL -> reject packet. No defaults or estimation.
 * 2. Strict Fetch Authorization: No match = no fetch. Never generate synthetic or default packets.
 * 3. UI5 Exclusive Packet Identity: Packets must originate from UI5. Preserve original identity.
 * 4. No Card-System Logic: Supports genuine NULL values; no 96-slot card auto-fill/looping.
 * 5. Accurate GPS and distance matching required.
 */
class TtsRootEngine(private val context: Context) {

    val ttsRootDir: File by lazy {
        com.example.myapplication01.TtsRepositoryManager.ensureTtsRepositoryInitialized(context)
    }

    private val scanner by lazy { TtsDirectoryScanner(ttsRootDir) }
    private val selector by lazy { TtsPacketSelector() }
    private val repositoryHandler by lazy { TtsRepositoryHandler(context) }

    private val executor = Executors.newSingleThreadExecutor()

    @Volatile
    private var currentPlayingPacket: TtsPacket? = null

    @Volatile
    private var preparedNextPacket: TtsPacket? = null

    @Volatile
    private var isPreparedFetching: Boolean = false

    private var localPlayer: TtsLocalPlayer? = null

    private var currentUserLat: Double? = null
    private var currentUserLng: Double? = null
    private var currentConfiguredDistanceKm: Double? = null

    init {
        initPlayer()
    }

    private fun initPlayer() {
        localPlayer = TtsLocalPlayer(
            context = context,
            onFirstWordSpoken = { _ ->
                triggerNextPreparation()
            },
            onPlaybackDone = { _ ->
                handlePlaybackCompletion()
            },
            onPlaybackError = { _ ->
                handlePlaybackCompletion()
            }
        )
    }

    /**
     * Pipeline entry point.
     * Rejects execution immediately if user GPS coordinates or distance data are missing or NULL.
     */
    fun executeTtsPipeline(
        userLat: Double?,
        userLng: Double?,
        configuredDistanceKm: Double? = 6.0
    ) {
        if (userLat == null || userLng == null || configuredDistanceKm == null) {
            // Null or unreadable GPS/distance data: reject action entirely
            return
        }
        if (userLat.isNaN() || userLng.isNaN() || configuredDistanceKm.isNaN() || configuredDistanceKm <= 0.0) {
            return
        }

        this.currentUserLat = userLat
        this.currentUserLng = userLng
        this.currentConfiguredDistanceKm = configuredDistanceKm

        executor.execute {
            if (localPlayer?.isPlaybackBusy == true || currentPlayingPacket != null) {
                triggerNextPreparation()
                return@execute
            }

            val selected = performFullScanAndSelect(userLat, userLng, configuredDistanceKm)
            if (selected != null) {
                playPacket(selected)
            }
        }
    }

    private fun performFullScanAndSelect(
        userLat: Double?,
        userLng: Double?,
        configuredDistanceKm: Double?
    ): TtsPacket? {
        if (userLat == null || userLng == null || configuredDistanceKm == null) return null

        // Rule 1: Full Directory Scan
        val candidates = scanner.scanAllPackets(userLat, userLng)

        // Rule 2 & Strict Validation: Match GPS & distance, select single packet
        return selector.selectSinglePacket(candidates, userLat, userLng, configuredDistanceKm)
    }

    private fun triggerNextPreparation() {
        val lat = currentUserLat
        val lng = currentUserLng
        val dist = currentConfiguredDistanceKm
        if (lat == null || lng == null || dist == null) return

        if (isPreparedFetching) return
        isPreparedFetching = true

        executor.execute {
            try {
                val nextSelected = performFullScanAndSelect(lat, lng, dist)

                val filteredNext = if (nextSelected != null && nextSelected.packetId == currentPlayingPacket?.packetId) {
                    null
                } else {
                    nextSelected
                }

                preparedNextPacket = filteredNext
            } finally {
                isPreparedFetching = false
            }
        }
    }

    private fun playPacket(packet: TtsPacket) {
        if (!packet.isDataValid()) {
            handlePlaybackCompletion()
            return
        }

        currentPlayingPacket = packet
        val textToSpeak = packet.text?.trim() ?: ""

        if (textToSpeak.isBlank()) {
            handlePlaybackCompletion()
            return
        }

        val success = localPlayer?.speakText(textToSpeak, packet.packetId) ?: false
        if (!success) {
            handlePlaybackCompletion()
        }
    }

    private fun handlePlaybackCompletion() {
        executor.execute {
            val finishedPacket = currentPlayingPacket
            currentPlayingPacket = null

            if (finishedPacket != null) {
                repositoryHandler.processPacketCountdown(finishedPacket) { _ ->
                    continueToNextPacket()
                }
            } else {
                continueToNextPacket()
            }
        }
    }

    private fun continueToNextPacket() {
        val next = preparedNextPacket
        preparedNextPacket = null

        if (next != null) {
            playPacket(next)
        } else {
            val candidate = performFullScanAndSelect(
                currentUserLat, currentUserLng, currentConfiguredDistanceKm
            )
            if (candidate != null) {
                playPacket(candidate)
            } else {
                releaseResources()
            }
        }
    }

    fun releaseResources() {
        localPlayer?.releaseAudioResources()
        localPlayer = null
        currentPlayingPacket = null
        preparedNextPacket = null
    }
}
