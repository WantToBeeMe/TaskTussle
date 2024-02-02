package me.wanttobee.tasktussle

import me.wanttobee.commandtree.ICommandNamespace
import me.wanttobee.commandtree.ICommandObject
import me.wanttobee.commandtree.nodes.*
import me.wanttobee.tasktussle.TaskTussleSystem.gameCommands
import me.wanttobee.tasktussle.TaskTussleSystem.minecraftPlugin
import me.wanttobee.tasktussle.TaskTussleSystem.taskManagers
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object TaskTussleCommand : ICommandNamespace {
    override val commandName: String = "taskTussle"
    override val commandSummary: String = "to start a game or change settings before starting the game"
    override val hasOnlyOneGroupMember: Boolean = false
    override val isZeroParameterCommand: Boolean = false
    override val systemCommands: Array<ICommandObject> = arrayOf(
        StartTree,
        StopTree,
        SettingsTree,
        DebugTree
    )

    object StartTree : ICommandObject {
        override val helpText: String = "to start one of the games"
        override val baseTree = CommandBranch("start",
            gameCommands.map { it.start }.toTypedArray()
        )
    }

    object DebugTree : ICommandObject {
        override val helpText: String = "check some information just about its current running game (no real usage besides that)"
        override val baseTree = CommandBranch("debug", arrayOf(
            CommandEmptyLeaf("status"){ commander ->
                TaskTussleSystem.debugStatus(commander)
            },
            CommandEmptyLeaf("log"){ commander ->
                TaskTussleSystem.canLog = !TaskTussleSystem.canLog
                commander.sendMessage("${TaskTussleSystem.title} look at the server console")
                if(TaskTussleSystem.canLog)
                    minecraftPlugin.logger.info("Started Logging")
                else minecraftPlugin.logger.info("Stopped Logging")
            },
        ))
    }

    object StopTree : ICommandObject {
        override val helpText: String = "to stop the current running game"
        override val baseTree = CommandEmptyLeaf("stop") { commander -> TaskTussleSystem.stopGame(commander) }
    }

    object SettingsTree : ICommandObject {
        override val helpText: String = "to view or change settings"

        private val genericSettings : Array<ICommandNode> = arrayOf(
            CommandBoolLeaf("chose_team_beforehand",
                { p, arg -> TaskTussleSystem.choseTeamsBeforehand = arg; settingIsChangedTo(p,"chose team beforehand", arg)  },
                { p -> settingIsCurrently(p,"chose team beforehand", TaskTussleSystem.choseTeamsBeforehand) }),
            CommandBoolLeaf("hide_card",
                { p, arg -> TaskTussleSystem.hideCard = arg; settingIsChangedTo(p,"hide card", arg) },
                { p -> settingIsCurrently(p,"hide card", TaskTussleSystem.hideCard) }),
            //CommandIntLeaf("tt_gameTime", 1, null,
            //    { p, arg -> TaskTussleSystem.gameTime = arg; p.sendMessage("${ChatColor.GOLD}gameTime${ChatColor.WHITE} is changed to: ${ChatColor.GOLD}$arg ${ChatColor.WHITE}minutes") },
            //    { p -> p.sendMessage("${ChatColor.GOLD}gameTime${ChatColor.WHITE} is currently: ${ChatColor.GOLD}${TaskTussleSystem.gameTime} ${ChatColor.WHITE}minutes") }),
            CommandIntLeaf("easy_ratio", 0, null,
                { p, arg -> TaskTussleSystem.easyRatio = arg; settingIsChangedTo(p,"easy-ratio","${ChatColor.GOLD}$arg${ChatColor.GRAY}/${TaskTussleSystem.normalRatio}/${TaskTussleSystem.hardRatio}") },
                { p -> settingIsCurrently(p,"easy-ratio","${ChatColor.GOLD}${TaskTussleSystem.easyRatio}${ChatColor.GRAY}/${TaskTussleSystem.normalRatio}/${TaskTussleSystem.hardRatio}") }),
            CommandIntLeaf("normal_ratio", 0, null,
                { p, arg -> TaskTussleSystem.normalRatio = arg; settingIsChangedTo(p,"normal-ratio","${ChatColor.GRAY}${TaskTussleSystem.easyRatio}/${ChatColor.GOLD}$arg${ChatColor.GRAY}/${TaskTussleSystem.hardRatio}") },
                { p -> settingIsCurrently(p,"normal-ratio","${ChatColor.GRAY}${TaskTussleSystem.easyRatio}/${ChatColor.GOLD}${TaskTussleSystem.normalRatio}${ChatColor.GRAY}/${TaskTussleSystem.hardRatio}") }),
            CommandIntLeaf("hard_ratio", 0, null,
                { p, arg -> TaskTussleSystem.hardRatio = arg; settingIsChangedTo(p,"hard-ratio","${ChatColor.GRAY}${TaskTussleSystem.easyRatio}/${TaskTussleSystem.normalRatio}/${ChatColor.GOLD}$arg") },
                { p -> settingIsCurrently(p,"hard-ratio","${ChatColor.GRAY}${TaskTussleSystem.easyRatio}/${TaskTussleSystem.normalRatio}/${ChatColor.GOLD}${TaskTussleSystem.hardRatio}") }),
        )

        override val baseTree = CommandBranch("settings", (arrayOf(
            CommandBranch("generic", genericSettings ),
            CommandBranch("games", gameCommands.map { CommandBranch(it.commandName, it.settings) }.toTypedArray() ),
            CommandBranch("tasks", taskManagers.map { CommandBranch(it.taskTypeName, it.settings) }.toTypedArray() ),
        )))

    }

    // the following 2 methods are to have a consistent message for checking and changing a setting
    fun settingIsCurrently(commander : Player, settingName: String, currentValue: Any, groupName : String = "") {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}${if(groupName != "") "($groupName)" else ""} ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is currently: ${ChatColor.GOLD}$currentValue")
    }
    fun settingIsChangedTo(commander : Player, settingName: String, newValue: Any, groupName : String = "") {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY}${if(groupName != "") "($groupName)" else ""} ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is changed to: ${ChatColor.GOLD}$newValue")
    }
}
