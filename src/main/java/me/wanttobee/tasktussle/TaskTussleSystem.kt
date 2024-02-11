package me.wanttobee.tasktussle

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarItem
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarSystem
import me.wanttobee.tasktussle.base.cards.ITTGameManager
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskFactory
import me.wanttobee.tasktussle.base.tasks.TaskEventsListener
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import me.wanttobee.tasktussle.teams.TeamSystem
import me.wanttobee.tasktussle.util.TimerSystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

object TaskTussleSystem {
    // some default configurations so that everything is split up
    lateinit var minecraftPlugin : JavaPlugin
        private set
    var title : String? = null
        private set

    val TaskTussleBaseFolder = File(MinecraftPlugin.instance.dataFolder, File.separator + "Tasks")

    // if this variable is null, that means there is no game active, otherwise the object that is stored here is the game
    private var gameSystem : ITTGameManager<*>? = null
    const val clickItemName = "Task Tussle Card"

    val clickItem = InteractiveHotBarItem(
        UniqueItemStack(Material.PAPER,"${ChatColor.GREEN}$clickItemName" , "${ChatColor.GRAY}right-click to open", true)
    ).setSlot(8).setRightClickEvent { player,_ -> openCard(player) }

    // this is for testing so the check if the teamAmount is to big is ignored
    // (and if it is, it will just result in empty teams, a bit ugly but everything still works)
    const val ignoreTeamSize = true
    var canLog = true
    fun log(message: Any){
        if(canLog) minecraftPlugin.logger.info(message.toString())
    }

    // if this is set to true, that means tasks cant be completed, they are all locked, until this is set to false again
    var completeTasksLocked = false
        private set
    private val gameTimerKey = "TaskTussle"

    //task tussle settings (common settings, settings that are for every game)
    var choseTeamsBeforehand = false
    var hideCard = false
    var easyRatio = 13
    var normalRatio = 8
    var hardRatio = 4
    var gameTime = 0


    fun initialize(plugin: JavaPlugin, title: String?) {
        minecraftPlugin = plugin
        plugin.server.pluginManager.registerEvents(TaskEventsListener, plugin)
        this.title = title
    }
    fun disablePlugin(){}

    // generates a bunch of tasks for the designated team
    // associatedTeam:
    //   Team -> That teams is the only team that can complete this task
    //   null -> all teams can complete this task (but tasks can still be completed only once of-coarse)
    // TODO:
    //  Instead of having `Team?` we could make it so we have `Array<Team>?`
    //  null would still mean the same, but it would mean that multiple teams can complete the task, but not all teams
    fun generateTasks(amount: Int, associatedTeam : Team?, associatedSet : TeamSet<*>, skips: List<ITask> = emptyList()) : Array<ITask>?{
        return TaskFactory.generateTasks(associatedTeam,associatedSet, amount, easyRatio, normalRatio, hardRatio, skips)
    }

    fun currentlyActiveGame() : Boolean{
        return gameSystem != null
    }

    // games must be started via here to ensure that everything runs correctly
    fun startGame(commander: Player, teamAmount : Int, game: ITTGameManager<*>){
        if(gameSystem != null ){
            commander.sendMessage("$title ${ChatColor.RED}there is already a game running")
            return
        }
        // we shuffle the task array before we start each game
        // we do this to ensure that the last task in the list doesn't have any big size (dis)advandate
        // we also only do this before the game that during task generation in the game, the shuffle of the types is the same throughout
        TaskTussleGrouper.taskManagers.shuffle()

        if(gameTime != 0)
            TimerSystem.createTimer(gameTimerKey, minecraftPlugin, gameTime*60, true) {
                gameSystem?.timeUpEnding()
            }

        gameSystem = game
        gameSystem!!.startGame(commander, teamAmount)
    }

    fun stopGame(commander: Player){
        if(gameSystem == null) {
            commander.sendMessage("$title ${ChatColor.RED}trying to stop a game that doesn't exists")
            return
        }
        if(gameSystem!!.clearGame())
            commander.sendMessage("$title ${ChatColor.GREEN}stopped the running game")
        else commander.sendMessage("$title ${ChatColor.YELLOW}stopped the running game, however, the game seemed to be broken")
        gameSystem = null
        // removing the things related to the gameTime
        TimerSystem.clearTimer(gameTimerKey)
        clickItem.removeFromEveryone()
        // we don't want to remove the clickItem, we only want it to be gone
        // the item itself is and stays correct at all times because we call openCard() which opens the newest game at all time
    }

    private fun openCard(p : Player){
        if(gameSystem == null) {
            p.sendMessage("$title ${ChatColor.RED}no game to open")
            return
        }
        val game = gameSystem!!.gameTeams ?: run {
            p.sendMessage("$title ${ChatColor.RED}no game to open")
            return
        }
        game.getObject(p)?.openCard(p)
    }

    fun debugStatus(commander : Player){
        commander.sendMessage("$title ${ChatColor.YELLOW}active taskTussle game:")
        if(gameSystem == null)
            commander.sendMessage("${ChatColor.GREEN}there is no game active")
        else{
            commander.sendMessage("${ChatColor.YELLOW}active: ${ChatColor.WHITE}${gameSystem!!::class.simpleName}")
            gameSystem!!.debugStatus(commander)
        }

        InteractiveInventorySystem.debugStatus(commander)
        InteractiveHotBarSystem.debugStatus(commander)
        TeamSystem.debugStatus(commander)
    }

    fun drawGame(commander : Player){
        if(gameSystem == null) {
            commander.sendMessage("$title ${ChatColor.RED}trying finished a game that doesn't exists")
            return
        }
        commander.sendMessage("$title ${ChatColor.GREEN}finished the running game")
        gameSystem!!.drawGame()
    }
    fun outOfTimeGame(commander: Player){
        if(gameSystem == null) {
            commander.sendMessage("$title ${ChatColor.RED}trying to finish a game that doesn't exists")
            return
        }
        commander.sendMessage("$title ${ChatColor.GREEN}finished the running game")
        gameSystem!!.timeUpEnding()
    }

    // these methods will really do anything when there is no game running,
    // sure it will lock the items, but that will be reset when the game starts anyway
    fun pauseGame(){
        completeTasksLocked = true
        TimerSystem.pauseTimer(gameTimerKey)
    }
    fun resumeGame(){
        completeTasksLocked = false
        TimerSystem.resumeTimer(gameTimerKey)
    }
}
