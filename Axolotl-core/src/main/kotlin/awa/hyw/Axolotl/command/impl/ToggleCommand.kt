package awa.hyw.Axolotl.command.impl

import awa.hyw.Axolotl.command.Command
import awa.hyw.Axolotl.module.ModuleManager
import awa.hyw.Axolotl.util.ChatUtil

class ToggleCommand : Command("t", ".t <module name>") {

    override fun run(args: Array<String>) {
        if (args.size != 2) {
            return ChatUtil.addMessageWithClient(usage)
        }

        val moduleName = args[1]
        val module = ModuleManager.getNullable(moduleName) ?:
        return ChatUtil.addMessageWithClient("Module not found: $moduleName")

        module.toggle()
        ChatUtil.addMessageWithClient("${module.getDisplayName()} is now ${if (module.enabled) "enabled" else "disabled"}")
    }

    override fun getSuggestions(args: Array<String>): List<String> {
        return when (args.size) {
            2 -> ModuleManager.modules().map { it.name }.filter { it.startsWith(args[1], ignoreCase = true) }
            else -> emptyList()
        }
    }
}


