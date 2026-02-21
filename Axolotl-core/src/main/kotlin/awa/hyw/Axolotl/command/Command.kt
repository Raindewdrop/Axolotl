package awa.hyw.Axolotl.command

abstract class Command(val name: String, val usage: String) {
    open fun run(args: Array<String>) {}
    open fun getSuggestions(args: Array<String>): List<String> = emptyList()
}


