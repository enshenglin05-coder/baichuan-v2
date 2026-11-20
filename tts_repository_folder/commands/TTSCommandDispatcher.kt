package com.example.tts.commands

/**
 * Explicit command dispatcher with an empty or strictly registered command registry.
 * Unknown commands must be rejected.
 */
class TTSCommandDispatcher {

    private val registry = mutableMapOf<String, (List<String>) -> Any?>()

    /**
     * Registers an explicit command handler.
     */
    fun registerCommand(commandName: String, handler: (List<String>) -> Any?) {
        registry[commandName] = handler
    }

    /**
     * Dispatches a command by name with arguments.
     * Unknown commands throw an IllegalArgumentException (rejected).
     */
    fun dispatch(commandName: String, args: List<String> = emptyList()): Any? {
        val handler = registry[commandName] ?: throw IllegalArgumentException("Unknown or unregistered command rejected: $commandName")
        return handler(args)
    }

    fun getRegisteredCommands(): Set<String> {
        return registry.keys.toSet()
    }
}
