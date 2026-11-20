package com.example.tts

import com.example.tts.commands.TTSCommandDispatcher
import com.example.tts.config.TTSConfig
import com.example.tts.runtime.TTSReader
import com.example.tts.runtime.TTSRootResolver
import com.example.tts.runtime.TTSRuntime
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class TTSMiniRootTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testRootResolverInitializationAndPathValidation() {
        val rootDir = tempFolder.newFolder("tts_repository_folder")
        val resolver = TTSRootResolver(rootDir)

        val resolvedRoot = resolver.resolveRoot()
        assertTrue(resolvedRoot.exists())
        assertEquals(rootDir.canonicalFile, resolvedRoot)

        // Permitted child path resolution
        val child = resolver.resolveChildPath("data")
        assertTrue(child.absolutePath.startsWith(resolvedRoot.absolutePath))

        // Reject traversal attempts
        assertThrows(SecurityException::class.java) {
            resolver.resolveChildPath("../outside.txt")
        }

        assertThrows(SecurityException::class.java) {
            resolver.resolveChildPath("/etc/passwd")
        }
    }

    @Test
    fun testTTSReaderPermittedAccess() {
        val rootDir = tempFolder.newFolder("tts_repository_folder")
        val dataDir = File(rootDir, "data").apply { mkdirs() }
        val testFile = File(dataDir, "sample.json").apply { writeText("{\"test\": true}") }

        val resolver = TTSRootResolver(rootDir)
        val reader = TTSReader(resolver)

        assertTrue(reader.checkRootExists())

        val files = reader.discoverFiles("data")
        assertEquals(1, files.size)
        assertEquals("sample.json", files[0].name)

        val content = reader.readFileContent("data/sample.json")
        assertTrue(content.contains("test"))

        // Unauthorized directory access
        assertThrows(SecurityException::class.java) {
            reader.discoverFiles("unauthorized_dir")
        }
    }

    @Test
    fun testCommandDispatcherRejectsUnknownCommands() {
        val dispatcher = TTSCommandDispatcher()
        assertTrue(dispatcher.getRegisteredCommands().isEmpty())

        // Unknown command must be rejected
        assertThrows(IllegalArgumentException::class.java) {
            dispatcher.dispatch("unknown_command", listOf("arg1"))
        }

        // Register and test valid command
        dispatcher.registerCommand("ping") { args ->
            "pong_${args.firstOrNull() ?: ""}"
        }

        val result = dispatcher.dispatch("ping", listOf("test"))
        assertEquals("pong_test", result)
    }

    @Test
    fun testTTSRuntimeInitialization() {
        val rootDir = tempFolder.newFolder("tts_repository_folder")
        val runtime = TTSRuntime(TTSRootResolver(rootDir))
        assertTrue(runtime.isHealthy())
        assertNotNull(runtime.commandDispatcher)
    }
}
