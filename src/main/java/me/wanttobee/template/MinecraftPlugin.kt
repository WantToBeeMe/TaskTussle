package me.wanttobee.template;

import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.plugin.java.annotation.command.Commands
import org.bukkit.plugin.java.annotation.dependency.Library
import org.bukkit.plugin.java.annotation.plugin.ApiVersion
import org.bukkit.plugin.java.annotation.plugin.Description
import org.bukkit.plugin.java.annotation.plugin.Plugin
import org.bukkit.plugin.java.annotation.plugin.author.Author


@Plugin(name = "Template", version ="1.0.1")
@ApiVersion(ApiVersion.Target.v1_20)
@Author("WantToBeeMe")
@Description("A super cool plugin")

@Commands(
       // Command(name = "helloWorld", aliases = ["hw","hello"], usage = "/helloWorld"),
       // Command(name = "byeWorld", aliases = ["bw","bye"], usage = "/byeWorld reason"),
)

// library has to be loaded in order to use kotlin
@Library("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.22") //kotlin !!
class MinecraftPlugin : JavaPlugin() {
    companion object {
        lateinit var instance: MinecraftPlugin
        val title = "${ChatColor.GRAY}[${ChatColor.GOLD}Template Plugin${ChatColor.GRAY}]${ChatColor.RESET}"
    }

    override fun onEnable() {
        instance = this

        // TODO: to set up this template you will have to change the "template" everywhere to your thing name
        //  so that is in:
        //  1. in settings.gradle
        //  2. folder/directory name
        //  3. @Plugin at the top of this class
        //  4. title in this companion object
        //  5, maybe. class name (instead of MinecraftPlugin, you could do TemplatePlugin)

        // Everything then should work still
        // yuo can build the plugin jar with the little hammer icon next to the run button
        // this would build it automatically, I think using the gradle settings (so you don't have to mess with artifact or anything)
        // the jar will be located in build/libs/Template-1.0.jar

        server.onlinePlayers.forEach { player ->
            player.sendMessage("$title Plugin has been enabled!")
        }
    }

    override fun onDisable() {
        server.onlinePlayers.forEach { player ->
            player.sendMessage("$title Plugin has been disabled!")
        }
    }
}
