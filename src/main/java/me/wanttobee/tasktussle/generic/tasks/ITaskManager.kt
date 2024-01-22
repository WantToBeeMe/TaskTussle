package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.commandTree.commandTree.CommandBoolLeaf
import me.wanttobee.commandTree.commandTree.ICommandNode
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.tasks.obtainTask.ObtainTaskManager
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.entity.Player

abstract class ITaskManager<T : ITask> {
    // taskEnabled boolean determent if this task will be added to the pool when generating the tasks
    var taskEnabled = true
        private set

    abstract val taskTypeName : String
    abstract val settings : Array<ICommandNode>

    fun getSettingNodes() : Array<ICommandNode>{
        return settings + CommandBoolLeaf("enabled",
            { p,arg -> taskEnabled = arg; ObtainTaskManager.settingIsChangedTo(p, "enabled", arg) },
            { p -> settingIsCurrently(p, "enabled",taskEnabled) })
    }

    // the following 2 methods are to have a consistent message for checking and changing a setting
    protected fun settingIsCurrently(commander : Player, settingName: String, currentValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}($taskTypeName) ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is currently: ${ChatColor.GOLD}$currentValue")
    }
    protected fun settingIsChangedTo(commander : Player, settingName: String, newValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}($taskTypeName) ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is changed to: ${ChatColor.GOLD}$newValue")
    }


    // this is to write your own implementation for generating tasks with the different difficulties
    abstract fun generateTasks(associatedTeam : Team, amounts : Triple<Int,Int,Int>, skip: List<ITask> = emptyList() ) : Array<T>?

    // clickItemName is the item that is used for the game, most of the time it's called TaskTussleCard,
    // but we don't want to be stuck to this name, so doing this we make sure that if we change it, it will be changed everywhere
    abstract fun getExplanationText(clickItemName : String) : String?
}
