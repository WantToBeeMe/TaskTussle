package me.wanttobee.tasktussle.games.bingov2

import me.wanttobee.tasktussle.TaskTussleGrouper
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.games.ITTGameManager
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskFactory
import me.wanttobee.tasktussle.games.bingo.BingoManager
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

object BingoManager : ITTGameManager<BingoTeam>(
    1..15,
    "Bingo", Material.FILLED_MAP,
    "Every team has a cube of 5x5 tasks, the first team to get a bingo wins") {
    // for bingo, we can just make the card here. but that might not be the case for other games
    override val defaultValue: (Team) -> BingoTeam = { t -> BingoTeam(BingoCardLogic(),t) }

    //settings
    var mutualTasks = 15 // the amount of tasks that are the same in across the different teams
    var winningCondition = "1 line"
    // "1 line","2 lines","3 lines", "4 lines","horizontal line","vertical line","diagonal line", "full card"

    override fun startGame(commander: Player, teams: TeamSet<BingoTeam>) {
        TaskTussleSystem.resumeGame()
        val messageColor = ChatColor.GREEN
        val itemName = "${ChatColor.GOLD}${TaskTussleSystem.clickItemName}$messageColor"
        teams.forEachPlayer { player ->
            player.sendMessage("${messageColor}Click with the $itemName in your hand to see your teams progress.")
            // we send all the explanations for the different tasks that are active (if they have it)
            // to tell the user how to complete curtain tasks if they don't know already
            for(taskManager in TaskTussleGrouper.taskManagers){
                if(taskManager.occupationRatio != 0 && taskManager.getExplanationText(itemName) != null){
                    player.sendMessage(
                        "${ChatColor.GRAY}(${taskManager.taskName}) $messageColor" +
                                taskManager.getExplanationText(itemName)
                    )
                }
            }
            player.sendMessage("${messageColor}The goal is set to: ${ChatColor.GOLD}${BingoManager.winningCondition}")
        }
        // val mutualTasksList = TaskTussleSystem.generateTasks(BingoManager.mutualTasks, null, teams)
        // if(mutualTasksList == null){
        //     commander.sendMessage("${ChatColor.RED}Cant start a game, task generation failed")
        //     return
        // }
        // teams.forEach { team, cardManager ->
        //     cardManager.setTeams(teams)
        //     val tasks : Array<ITask>
        //     if(BingoManager.mutualTasks == 25){
        //         tasks = TaskFactory.combineTasks(mutualTasksList, emptyArray(), team)
        //     }
        //     else if(BingoManager.mutualTasks < 25){
        //         val seperatedTasks = TaskTussleSystem.generateTasks(25- BingoManager.mutualTasks, team, teams, mutualTasksList.toList())
        //         if(seperatedTasks == null){
        //             commander.sendMessage("${ChatColor.RED}Cant start a game, task generation failed")
        //             return@forEach
        //         }
        //         tasks = TaskFactory.combineTasks(mutualTasksList, seperatedTasks, team)
        //     }
        //     else{
        //         mutualTasksList.shuffle()
        //         // there are more mutual tasks than there can be in 1 card.
        //         // That means that the mutual tasks represent a smaller pool where all players take from
        //         // that also means we have to shuffle the pool before we take from it
        //         tasks = TaskFactory.combineTasks(mutualTasksList.take(25).toTypedArray(), emptyArray(), team)
        //     }
        //     tasks.shuffle()
        //     cardManager.setTasks(tasks)
        //     team.forEachMember { member -> cardManager.openCard(member) }
        // }
    }


    override fun finishGame(winningTeam: Team) {
        TODO("Not yet implemented")
    }

    override fun drawGame() {
        TODO("Not yet implemented")
    }




}