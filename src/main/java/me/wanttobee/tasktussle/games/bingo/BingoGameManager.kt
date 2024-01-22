package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.cards.ITTGameManager
import me.wanttobee.tasktussle.generic.tasks.TaskFactory
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player

object BingoGameManager : ITTGameManager<BingoCard> {
    override var gameTeams: TeamSet<BingoCard>? = null
    override val defaultValue: (Team) -> BingoCard = { t -> BingoCard(t)}
    override val teamRange: IntRange = 1..10
    val winConditions = arrayOf("1_line","2_lines","3_lines","horizontal_line","vertical_line","diagonal_line", "full_card")

    //settings
    var mutualTasks = 15 // the amount of tasks that are the same in across the different teams
    var winningCondition = "1_line"

    override fun startGame(commander: Player, teams: TeamSet<BingoCard>) {
        TaskTussleSystem.completeTasksLocked = false // we turn of the taskLock (so tasks can be completed again)
        val messageColor = ChatColor.GREEN
        val itemName = "${ChatColor.GOLD}${TaskTussleSystem.clickItemName}$messageColor"
        teams.forEachPlayer { player ->
            player.sendMessage("${messageColor}Click with the $itemName in your hand to see your teams progress.")
            // we send all the explanations for the different tasks that are active (if they have it)
            // to tell the user how to complete curtain tasks if they don't know already
            for(taskManager in TaskTussleSystem.taskManagers){
                if(taskManager.taskEnabled && taskManager.getExplanationText(itemName) != null){
                    player.sendMessage(
                        "${ChatColor.GRAY}(${taskManager.taskTypeName})$messageColor" +
                                taskManager.getExplanationText(itemName)
                    )
                }
            }
            player.sendMessage("${messageColor}The goal is set to: ${ChatColor.GOLD}$winningCondition")
        }

        val mutualTasksList = TaskTussleSystem.getTasks(Team(0), mutualTasks)
        if(mutualTasksList == null){
            commander.sendMessage("${ChatColor.RED}Cant start a game, task generation failed")
            return
        }
        teams.forEach { team, cardManager ->
            cardManager.setTeams(teams)
            cardManager.setTasks(TaskTussleSystem.getTasks(team,25)!!)
            if(mutualTasks == 25){
                mutualTasksList.shuffle()
                cardManager.setTasks(TaskFactory.combineTasks(mutualTasksList, emptyArray(), team))
            }
            else if(mutualTasks < 25){
                val seperatedTasks = TaskTussleSystem.getTasks(team,25-mutualTasks)
                if(seperatedTasks == null){
                    commander.sendMessage("${ChatColor.RED}Cant start a game, there are not enough tasks to make it (need 25)")
                    return@forEach
                }
                val tasks = TaskFactory.combineTasks(mutualTasksList, seperatedTasks, team)
                tasks.shuffle()
                cardManager.setTasks(tasks)
            }
            else{
                mutualTasksList.shuffle()
                cardManager.setTasks(TaskFactory.combineTasks(mutualTasksList.take(25).toTypedArray(), emptyArray(), team))
            }
            team.forEachMember { member -> cardManager.openCard(member) }
        }
    }

    override fun finishGame(winningTeam: Team) {
        TaskTussleSystem.completeTasksLocked = true

        if(TaskTussleSystem.hideCard){
            gameTeams!!.forEachObject { cardManager ->
                cardManager.card.teamIcon.setClickable(true)
            }
        }
        gameTeams!!.forEachPlayer  { p ->
            gameTeams!!.getObject(winningTeam)?.openCard(p)
            p.playSound(p.location, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.MASTER, 0.2f, 1f)
            p.playSound(p.location, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundCategory.MASTER, 0.9f, 2f)
            p.playSound(p.location, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 1f, 1f)
        }
    }

    fun checkCardForWin(cardManager: BingoCard) {
        val completed = cardManager.getCompletedLines()
        if(gameTeams == null) return
        val sum = completed.first + completed.second + completed.third
        val finished = when(winningCondition){
            "horizontal_line" -> completed.first >= 1
            "vertical_line" -> completed.second >= 1
            "diagonal_line" -> completed.third >= 1
            "1_line" -> sum >= 1
            "2_lines" -> sum >= 2
            "3_lines" -> sum >= 3
            "full_card" -> completed.first == 5
            else -> false
        }
        if(finished) finishGame(gameTeams!!.getTeam(cardManager)!!)
    }
}
