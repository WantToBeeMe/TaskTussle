package me.wanttobee.tasktussle.commands

import me.wanttobee.tasktussle.MinecraftPlugin
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

object WTBMCommands {
    private lateinit var minecraftPlugin : JavaPlugin
    var title : String? = null
        private set

    fun setPlugin(plugin: JavaPlugin, title: String?){
        minecraftPlugin = plugin
        this.title = title
    }

    fun createCommand(command : String,  commandObject : IPlayerCommands){
        minecraftPlugin.getCommand(command)?.setExecutor(commandObject)
        minecraftPlugin.getCommand(command)?.tabCompleter = commandObject
    }
    fun createCommand(commandObject : ICommandNamespace){
        minecraftPlugin.getCommand(commandObject.commandName)?.setExecutor(commandObject)
        minecraftPlugin.getCommand(commandObject.commandName)?.tabCompleter = commandObject
    }

    fun sendErrorToSender(sender: Player, errorMessage: String, extraInfo : String = ""){
        val titleBit = title ?: ""
        sender.sendMessage("$titleBit ${ChatColor.RED}$errorMessage ${ChatColor.GRAY}$extraInfo")
    }

}