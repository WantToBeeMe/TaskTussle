package me.wanttobee.tasktussle

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarItem
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarSystem
import me.wanttobee.tasktussle.generic.cards.ITTGameManager
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.ITaskManager
import me.wanttobee.tasktussle.generic.tasks.TaskFactory
import me.wanttobee.tasktussle.generic.tasks.TaskEventsListener
import me.wanttobee.tasktussle.tasks.obtainTask.ObtainTaskManager
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSystem
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

    //task tussle settings (common settings, settings that are for every game)
    var choseTeamsBeforehand = false
    var gameTime = 0
    var hideCard = false
    var easyRatio = 13
    var normalRatio = 8
    var hardRatio = 4

    fun initialize(plugin: JavaPlugin, title: String?) {
        minecraftPlugin = plugin
        plugin.server.pluginManager.registerEvents(TaskEventsListener, plugin)
        this.title = title
    }
    fun disablePlugin(){}

    fun getTasks(associatedTeam : Team, amount: Int, skips: List<ITask> = emptyList()) : Array<ITask>?{
        return TaskFactory.createTasks(associatedTeam, amount, easyRatio, normalRatio, hardRatio, skips)
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

        gameSystem = game
        gameSystem!!.startGame(commander, teamAmount)
    }

    fun stopGame(commander: Player){
        if(gameSystem == null) {
            commander.sendMessage("$title ${ChatColor.RED}trying to stop a game that doesn't exists")
            return
        }
        if(gameSystem!!.endGame())
            commander.sendMessage("$title ${ChatColor.GREEN}stopped the running game")
        else commander.sendMessage("$title ${ChatColor.YELLOW}stopped the running game, however, the game seemed to be broken")
        gameSystem = null
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
}
