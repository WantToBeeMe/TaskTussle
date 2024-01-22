package me.wanttobee.tasktussle.generic.cards

import me.wanttobee.commandTree.commandTree.ICommandNode
import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// TaskTussle GAME COMMANDS
abstract class ITTGameCommands {
    abstract val commandName: String

    abstract val settings : Array<ICommandNode>
    abstract val start: ICommandNode


    protected fun settingIsCurrently(commander : Player, settingName: String, currentValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}($commandName) ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is currently: ${ChatColor.GOLD}$currentValue")
    }
    protected fun settingIsChangedTo(commander : Player, settingName: String, newValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}($commandName) ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is changed to: ${ChatColor.GOLD}$newValue")
    }
}