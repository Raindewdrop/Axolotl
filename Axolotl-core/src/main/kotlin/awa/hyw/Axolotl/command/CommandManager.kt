package awa.hyw.Axolotl.command

import org.apache.logging.log4j.LogManager
import awa.hyw.Axolotl.command.impl.BindCommand
import awa.hyw.Axolotl.command.impl.SkinCommand
import awa.hyw.Axolotl.command.impl.ToggleCommand
import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.SendMessageEvent

object CommandManager {
    private val log = LogManager.getLogger(javaClass)

    private val commands by lazy {
        listOf(
        BindCommand(),
        ToggleCommand(),
        SkinCommand()
        )
    }

    private fun runCommand(message: String): Boolean {
        val args = message.split(' ')
        log.debug(args.toString())
        val commandName = args[0].removePrefix(".")
        return commands.firstOrNull { it.name.lowercase() == commandName.lowercase() }?.let {
            it.run(args.toTypedArray())
            true
        } ?: false
    }

    fun getSuggestions(message: String): List<String> {
        val args = message.split(" ")
        val commandName = args[0].removePrefix(".")

        if (args.size == 1) {
            return commands
                .map { "." + it.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
        }

        val command = commands.find { it.name.equals(commandName, ignoreCase = true) } ?: return emptyList()
        return command.getSuggestions(args.toTypedArray())
    }

    @EventTarget
    fun onChat(event: SendMessageEvent) {
        if (event.message.startsWith(".") && runCommand(event.message)) {
            event.isCancelled = true
        }
    }
}


