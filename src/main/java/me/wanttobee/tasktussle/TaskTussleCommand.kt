package me.wanttobee.tasktussle

import me.wanttobee.commandTree.ICommandNamespace
import me.wanttobee.commandTree.ICommandObject
import me.wanttobee.commandTree.commandTree.*
import me.wanttobee.tasktussle.TaskTussleSystem.gameCommands
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

    //  Todo:
    //   /taskTussle setting generic ...
    //   /taskTussle setting games  ...
    //   /taskTussle setting tasks  ...
    //   /taskTussle start ...
    //   /taskTussle stop
    //   /taskTussle debug
    //   E.G.
    //   /taskTussle settings generic gameTime 15
    //   /taskTussle settings games bingo winCondition full_card
    //   /taskTussle start bingo 3
    //   /taskTussle stop

    object StartTree : ICommandObject {
        override val helpText: String = "to start one of the games"
        override val baseTree = CommandBranch("start", 
            gameCommands.map { it.start }.toTypedArray()
        )
    }
    object DebugTree : ICommandObject {
        override val helpText: String = "check some information just about its current running game (no real usage besides that)"
        override val baseTree = CommandEmptyLeaf("debug") { commander ->
            TaskTussleSystem.debugStatus(commander)
        }
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

    private fun settingIsCurrently(commander : Player, settingName: String, currentValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY} ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is currently: ${ChatColor.GOLD}$currentValue")
    }
    private fun settingIsChangedTo(commander : Player, settingName: String, newValue: Any) {
        commander.sendMessage("${TaskTussleSystem.title}${ChatColor.GRAY} ${ChatColor.GOLD}$settingName${ChatColor.WHITE} is changed to: ${ChatColor.GOLD}$newValue")
    }

}