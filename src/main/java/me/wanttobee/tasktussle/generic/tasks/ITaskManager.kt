package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.commandTree.commandTree.ICommandNode
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.entity.Player

abstract class ITaskManager<T : ITask> {
    abstract fun isEnabled() : Boolean
    // this is to write your own implementation for generating tasks with the different difficulties
    abstract fun generateTasks(associatedTeam : Team, amounts : Triple<Int,Int,Int>, skip: List<ITask> = emptyList() ) : Array<T>?

    abstract val taskTypeName : String
    abstract val settings : Array<ICommandNode>
    protected fun settingIsCurrently(commander : Player, settingName: String, currentValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}($taskTypeName) ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is currently: ${ChatColor.GOLD}$currentValue")
    }
    protected fun settingIsChangedTo(commander : Player, settingName: String, newValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}($taskTypeName) ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is changed to: ${ChatColor.GOLD}$newValue")
    }
}