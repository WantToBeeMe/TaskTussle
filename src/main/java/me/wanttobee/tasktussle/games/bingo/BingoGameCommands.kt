package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.commandtree.nodes.CommandIntLeaf
import me.wanttobee.commandtree.nodes.CommandStringLeaf
import me.wanttobee.commandtree.nodes.ICommandNode
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.cards.ITTGameCommands
import org.bukkit.ChatColor

object BingoGameCommands : ITTGameCommands() {
    override val commandName: String = "bingo"
    override val settings: Array<ICommandNode> = arrayOf(
        CommandStringLeaf("win_condition", BingoGameManager.winConditions,
            { p,arg -> BingoGameManager.winningCondition = arg; settingIsChangedTo(p,"win condition", arg) },
            { p -> settingIsCurrently(p, "win condition", BingoGameManager.winningCondition) }),
        CommandIntLeaf("mutual_tasks", 0, null,
            { p,arg -> BingoGameManager.mutualTasks = arg; settingIsChangedTo(p,"mutual tasks", arg) },
            { p -> settingIsCurrently(p,"mutual tasks", BingoGameManager.mutualTasks) }),
    )
    override val start: ICommandNode = CommandIntLeaf(commandName, 1, 10,
        {commander, size -> TaskTussleSystem.startGame(commander,size,BingoGameManager) },
        {commander -> commander.sendMessage("${ChatColor.RED}you must specify the amount of teams you want to play with") }
    )
}
