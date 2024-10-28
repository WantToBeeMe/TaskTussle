package me.wanttobee.tasktussle;

import me.wanttobee.everythingitems.ItemUtil
import me.wanttobee.commandtree.CommandTreeSystem
import me.wanttobee.tasktussle.base.generic.TaskTussleConfig
import me.wanttobee.tasktussle.teams.TeamSystem
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin

class MinecraftPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: MinecraftPlugin
        val title = "${ChatColor.GRAY}[${ChatColor.GOLD}Task Tussle${ChatColor.GRAY}]${ChatColor.RESET}"
    }

    override fun onEnable() {
        instance = this
        CommandTreeSystem.initialize(instance, "${ChatColor.GREEN}(C)$title")
        ItemUtil.initialize(instance, "${ChatColor.LIGHT_PURPLE}(I)$title")
        TeamSystem.initialize(instance, "${ChatColor.AQUA}(T)$title")
        TaskTussleSystem.initialize(instance, "${ChatColor.YELLOW}(B)$title")

        TaskTussleGrouper.generateDefaultFolders()
        // CommandTreeSystem.createCommand(DebugCommand)
        CommandTreeSystem.createCommand(TaskTussleConfig)

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
