package me.wanttobee.tasktussle;

import me.wanttobee.everythingitems.ItemUtil
import me.wanttobee.commandtree.CommandTreeSystem
import me.wanttobee.tasktussle.commands.DebugCommands
import me.wanttobee.tasktussle.commands.WorldSetupCommands
import me.wanttobee.tasktussle.commands.TaskTussleCommands
import me.wanttobee.tasktussle.commands.TeamCommands
import me.wanttobee.tasktussle.teams.TeamSystem
import me.wanttobee.tasktussle.util.WorldSetupHelper
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

class MinecraftPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: MinecraftPlugin
        val title = "${ChatColor.GRAY}[${ChatColor.GOLD}Task Tussle${ChatColor.GRAY}]${ChatColor.RESET}"
    }

    override fun onEnable() {
        instance = this
        CommandTreeSystem.initialize(instance, "${ChatColor.GREEN}(Com)$title")
        ItemUtil.initialize(instance, "${ChatColor.LIGHT_PURPLE}(Ite)$title")
        TeamSystem.initialize(instance, "${ChatColor.AQUA}(Tea)$title")
        TaskTussleSystem.initialize(instance, "${ChatColor.YELLOW}(TT)$title")
        server.pluginManager.registerEvents(WorldSetupHelper, this)

        TaskTussleGrouper.generateDefaultFolders()
        CommandTreeSystem.createCommand(DebugCommands)
        CommandTreeSystem.createCommand(TaskTussleCommands)
        CommandTreeSystem.createCommand(TeamCommands)
        CommandTreeSystem.createCommand(WorldSetupCommands)

        server.onlinePlayers.forEach { player ->
            player.sendMessage("$title Plugin has been enabled!")
        }
    }

    override fun onDisable() {
        ItemUtil.disablePlugin()
        TeamSystem.disablePlugin()
        TaskTussleSystem.disablePlugin()

        server.onlinePlayers.forEach { player ->
            player.sendMessage("$title Plugin has been disabled!")
        }
    }
}
