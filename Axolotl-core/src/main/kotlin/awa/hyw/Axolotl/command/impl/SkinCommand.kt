package awa.hyw.Axolotl.command.impl

import awa.hyw.Axolotl.command.Command
import awa.hyw.Axolotl.config.ConfigManager
import awa.hyw.Axolotl.config.impl.SkinManager
import awa.hyw.Axolotl.util.ChatUtil

class SkinCommand : Command("skin", ".skin <player_name>") {
    override fun run(args: Array<String>) {
        if (args.size < 2) {
            ChatUtil.addMessageWithClient("Usage: .skin <player_name>")
            return
        }

        val playerName = args[1]

        SkinManager.loadSkin(playerName)
        
        ConfigManager.saveConfig("Skin")
        
        ChatUtil.addMessageWithClient("Skin changed to $playerName")
    }

    override fun getSuggestions(args: Array<String>): List<String> {
        return emptyList()
    }
}
