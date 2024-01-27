package me.wanttobee.tasktussle;

import me.wanttobee.everythingitems.ItemUtil
import me.wanttobee.commandtree.CommandTreeSystem
import me.wanttobee.tasktussle.tasks.obtainTask.ObtainTaskFiles
import me.wanttobee.tasktussle.teams.TeamSystem
import me.wanttobee.tasktussle.tests.DebugCommand
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.annotation.command.Command
import org.bukkit.plugin.java.annotation.command.Commands
import org.bukkit.plugin.java.annotation.dependency.Library
import org.bukkit.plugin.java.annotation.plugin.ApiVersion
import org.bukkit.plugin.java.annotation.plugin.Description
import org.bukkit.plugin.java.annotation.plugin.Plugin
import org.bukkit.plugin.java.annotation.plugin.author.Author


@Plugin(name = "TaskTussle", version ="1.0.1")
@ApiVersion(ApiVersion.Target.v1_20)
@Author("WantToBeeMe")
@Description("A plugin with a lot of possibilities for all kinds of task based games")

@Commands(
    //Command(name = "helloWorld", aliases = ["hw","hello"], usage = "/helloWorld"),
    Command(name = "debug_tt", usage = "/debug_tt"),
    Command(name = "taskTussle", aliases = ["tt"], usage = "/taskTussle")
)

// library has to be loaded in order to use kotlin
@Library("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22") //kotlin !!
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

        ObtainTaskFiles.generateDefaultFolder()
        // WTBMCommands.createCommand(HelloWorldCommands)
        // WTBMCommands.createCommand(InventoryTestCommands)
        // WTBMCommands.createCommand(TeamCommands)
        CommandTreeSystem.createCommand(DebugCommand)
        CommandTreeSystem.createCommand(TaskTussleCommand)

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
