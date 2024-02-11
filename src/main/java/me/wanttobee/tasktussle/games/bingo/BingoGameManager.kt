package me.wanttobee.tasktussle.games.bingo

import jdk.swing.interop.DragSourceContextWrapper
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.generic.TaskTussleSettings
import me.wanttobee.tasktussle.base.cards.ITTGameManager
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskFactory
import me.wanttobee.tasktussle.TaskTussleGrouper
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import kotlin.math.max

object BingoGameManager : ITTGameManager<BingoCard>( 1..15, "Bingo", Material.FILLED_MAP,
    "Every team has a cube of 5x5 tasks, the first team to get a bingo wins") {
    override val defaultValue: (Team) -> BingoCard = { t -> BingoCard(t)}

    //settings
    var mutualTasks = 15 // the amount of tasks that are the same in across the different teams
    var winningCondition = "1 line"
    // "1 line","2 lines","3 lines", "4 lines","horizontal line","vertical line","diagonal line", "full card"

    init{
        // note that if you change option here, you also have to change it at the overTimeMethod
        addOvertimeSetting(arrayOf("draw","most tasks wins"))

        // mutual tasks
        val mutualTaskLore = listOf(
            "${ChatColor.DARK_GRAY}L Click: ${ChatColor.GRAY}Increase amount",
            "${ChatColor.DARK_GRAY}R Click: ${ChatColor.GRAY}Decrease amount",
            "${ChatColor.DARK_GRAY}Shift+L Click: ${ChatColor.GRAY}Set amount to 25",
            "${ChatColor.DARK_GRAY}Shift+R Click: ${ChatColor.GRAY}Set amount to 0")
        val mutualTasksIcon = UniqueItemStack(Material.ENDER_PEARL,"", mutualTaskLore)
        settingsInventory.addSetting(mutualTasksIcon,{
            val newTitle = "${TaskTussleSettings.settingColor}"+
                    (if(mutualTasks <= 25) "Mutual tasks" else "Task pool") +
                    ":${ChatColor.YELLOW} $mutualTasks"
            mutualTasksIcon
                .updateTitle(newTitle)
                .updateMaterial(if(mutualTasks <= 25) Material.ENDER_PEARL else Material.ENDER_EYE)
                .updateEnchanted(mutualTasks > 0)
                .updateCount(max(1, mutualTasks))
                .pushUpdates()
        },{_,shift ->
            if(shift) mutualTasks = 25
            else mutualTasks += 1
        },{_,shift ->
            mutualTasks = if(shift) 0
            else max(0, mutualTasks-1)
        })

        // win condition
        val winConditionIcon = UniqueItemStack(Material.AMETHYST_SHARD,"", null)
            .updateEnchanted(true)
        settingsInventory.addSetting(winConditionIcon, {
            winConditionIcon.updateTitle(
                "${TaskTussleSettings.settingColor}Win condition: ${ChatColor.YELLOW}$winningCondition"
            ).pushUpdates()
        }){ p,_ -> WinConditionPicker().open(p) }
    }

    override fun startGame(commander: Player, teams: TeamSet<BingoCard>) {
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
            player.sendMessage("${messageColor}The goal is set to: ${ChatColor.GOLD}$winningCondition")
        }

        val mutualTasksList = TaskTussleSystem.generateTasks(mutualTasks, null, teams)
        if(mutualTasksList == null){
            commander.sendMessage("${ChatColor.RED}Cant start a game, task generation failed")
            return
        }
        teams.forEach { team, cardManager ->
            cardManager.setTeams(teams)
            val tasks : Array<ITask>
            if(mutualTasks == 25){
                tasks = TaskFactory.combineTasks(mutualTasksList, emptyArray(), team)
            }
            else if(mutualTasks < 25){
                val seperatedTasks = TaskTussleSystem.generateTasks(25-mutualTasks, team, teams, mutualTasksList.toList())
                if(seperatedTasks == null){
                    commander.sendMessage("${ChatColor.RED}Cant start a game, task generation failed")
                    return@forEach
                }
                tasks = TaskFactory.combineTasks(mutualTasksList, seperatedTasks, team)
            }
            else{
                mutualTasksList.shuffle()
                // there are more mutual tasks than there can be in 1 card.
                // That means that the mutual tasks represent a smaller pool where all players take from
                // that also means we have to shuffle the pool before we take from it
                tasks = TaskFactory.combineTasks(mutualTasksList.take(25).toTypedArray(), emptyArray(), team)
            }
            tasks.shuffle()
            cardManager.setTasks(tasks)
            team.forEachMember { member -> cardManager.openCard(member) }
        }
    }

    private fun endGame(){
        if(TaskTussleSystem.hideCard){
            gameTeams!!.forEachObject { card ->
                card.cardGui.teamIcon.setClickable(true)
            }
        }
        gameTeams!!.forEachObject { card ->
            card.showContributions()
        }
    }
    override fun finishGame(winningTeam: Team) {
        TaskTussleSystem.pauseGame()
        endGame()

        // everyone opening the winning card
        gameTeams!!.forEachPlayer  { p ->
            gameTeams!!.getObject(winningTeam)?.openCard(p)
            p.sendMessage("${winningTeam.getDisplayName()}${ChatColor.GREEN} Won the game")
            p.playSound(p.location, Sound.BLOCK_END_PORTAL_SPAWN, SoundCategory.MASTER, 0.2f, 1f)
            p.playSound(p.location, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, SoundCategory.MASTER, 0.9f, 2f)
            p.playSound(p.location, Sound.BLOCK_BEACON_POWER_SELECT, SoundCategory.MASTER, 1f, 0.9f)
        }
    }

    override fun drawGame() {
        TaskTussleSystem.pauseGame()
        endGame()

        // everyone open their own card
        gameTeams!!.forEach  { team, card ->
            team.forEachMember { p ->
                card.openCard(p)
                p.sendMessage("${ChatColor.YELLOW}The game drew")
                p.playSound(p.location, Sound.BLOCK_SCULK_CATALYST_PLACE, SoundCategory.MASTER, 0.5f, 0.7f)
                p.playSound(p.location, Sound.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.MASTER, 0.6f, 0.5f)
                p.playSound(p.location, Sound.ENTITY_ALLAY_ITEM_GIVEN, SoundCategory.MASTER, 1f, 0.5f)
            }
        }
    }


    override fun timeUpEnding() {
        if(atOvertimeSetting == "most tasks wins"){
            var currentBestCount = -1
            var currentBestTeam : Team? = null
            gameTeams!!.forEach {team, card ->
                val thisCompleted = card.getCompletedAmount()
                if(thisCompleted == currentBestCount){
                    currentBestTeam = null
                }
                if(thisCompleted > currentBestCount){
                    currentBestCount = thisCompleted
                    currentBestTeam = team
                }
            }
            if(currentBestTeam != null){
                finishGame(currentBestTeam!!)
                return
            }
        }
        drawGame()
    }


    fun checkCardForWin(cardManager: BingoCard) {
        val completed = cardManager.getCompletedLines()
        if(gameTeams == null) return
        val sum = completed.first + completed.second + completed.third
        val finished = when(winningCondition){
            "horizontal line" -> completed.first >= 1
            "vertical line" -> completed.second >= 1
            "diagonal line" -> completed.third >= 1
            "1 line" -> sum >= 1
            "2 lines" -> sum >= 2
            "3 lines" -> sum >= 3
            "4 lines" -> sum >= 4
            "full card" -> completed.first == 5
            else -> false
        }
        if(finished) finishGame(gameTeams!!.getTeam(cardManager)!!)
    }
}
