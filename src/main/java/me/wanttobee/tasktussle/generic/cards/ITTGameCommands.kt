package me.wanttobee.tasktussle.generic.cards

import me.wanttobee.commandTree.commandTree.ICommandNode
import me.wanttobee.tasktussle.TaskTussleCommand
import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// TaskTussle GAME COMMANDS
abstract class ITTGameCommands {
    abstract val commandName: String

    abstract val settings : Array<ICommandNode>
    abstract val start: ICommandNode

    // the following 2 methods are to have a consistent message for checking and changing a setting
    protected fun settingIsCurrently(commander : Player, settingName: String, currentValue: Any) {
        TaskTussleCommand.settingIsCurrently(commander,settingName,currentValue,commandName)
    }
    protected fun settingIsChangedTo(commander : Player, settingName: String, newValue: Any) {
        TaskTussleCommand.settingIsChangedTo(commander,settingName,newValue,commandName)
    }
}
