package me.wanttobee.tasktussle.base.games

import me.wanttobee.commandtree.nodes.CommandEmptyLeaf
import me.wanttobee.commandtree.nodes.CommandIntLeaf
import me.wanttobee.commandtree.nodes.ICommandNode
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.tasktussle.TaskTussleGrouper
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.generic.IManager
import me.wanttobee.tasktussle.base.generic.ManagerSettings
import me.wanttobee.tasktussle.base.generic.TaskTussleSettings
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import me.wanttobee.tasktussle.teams.TeamSystem
import me.wanttobee.tasktussle.util.toLore
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.max
import kotlin.math.min

abstract class ITTGameManager <T : ITTGameTeam>(
    private val teamRange: IntRange, val gameName: String, gameIconMaterial: Material,
    gameDescription: String, settingsRows : Int = 1) :
    IManager(gameIconMaterial, gameName, gameDescription) {
    // if there is a no game active, this set is null
    var gameTeams : TeamSet<T>? = null
    val settingsInventory = ManagerSettings(this, settingsRows)

    var gameFinished = false
        protected set

    // this settings may be used for a lot more games, therefore its in the base class
    protected var atOvertimeSetting : String? = null

    // to make sure that whenever we start game, we have the default already predefined, and you don't have to anymore
    abstract val defaultValue : ((Team) -> T)
    val startCommand : ICommandNode = if( teamRange.min() != teamRange.max() )
        CommandIntLeaf(
            gameName.lowercase().replace(' ', '_'), teamRange.min(), teamRange.max(),
            {commander, size -> TaskTussleSystem.startGame(commander, size, this) },
            {commander -> commander.sendMessage("${ChatColor.RED}you must specify the amount of teams you want to play with") }
        ) else
        CommandEmptyLeaf(gameName.lowercase().replace(' ', '_')) { commander ->
            TaskTussleSystem.startGame(commander,  teamRange.min(), this)
        }

    // this method will eventually call startGame(commander, gameSet)
    // when the game has been made
    fun startGame(commander: Player, teamAmount : Int) : Boolean{
        if(teamAmount !in teamRange){
            commander.sendMessage("${TaskTussleSystem.title} ${ChatColor.RED}team amount invalid, this game can have from ${teamRange.first} up to ${teamRange.last} teams total")
            return false
        }
        if(gameTeams != null)  {
            commander.sendMessage("${TaskTussleSystem.title} ${ChatColor.RED}this game is already running")
            return false
        }
        val playerCount = TaskTussleSystem.minecraftPlugin.server.onlinePlayers.size
        if(teamAmount > playerCount && !TaskTussleSystem.ignoreTeamSize) {
            commander.sendMessage("${TaskTussleSystem.title} ${ChatColor.RED}there are not enough players online to make${ChatColor.GRAY } $teamAmount ${ChatColor.RED}teams")
            return false
        }
        // from here we are safe because we checked what could all go wrong, we can now safely create the teams and start the game

        val startEffect :(TeamSet<T>) -> Unit = { set ->
            gameTeams = set
            set.forEachPlayer { player -> TaskTussleSystem.clickItem.giveToPlayer(player) }
            // we make sure that if a task type wants to set something before the game starts, that we give it the time
            // for example, advancements task needs all advancements to be cleared otherwise they can't be completed anymore
            for(manager in TaskTussleGrouper.taskManagers)
                manager.prepareForThisTaskType(set)

            set.forEachObject { teamObject -> teamObject.associatedCard?.selectCardGui(set) }
            startGame(commander,set)
        }

        // if we want to choose the teams beforehand, we need to start the team maker and tell the team maker what to do with those teams
        // if we don't want to choose, we can just generate the teams directly
        if(TaskTussleSystem.choseTeamsBeforehand)
            TeamSystem.startTeamMaker(commander,defaultValue,teamAmount, gameName, startEffect)
        else startEffect.invoke(TeamSystem.generateTeams(teamAmount, gameName, defaultValue))
        return true
    }

    // this startGame version is called when the important things are already assigned,
    // so you only have to override this and do the last specific game related things
    abstract fun startGame(commander: Player, teams: TeamSet<T>)

    // this method will be called whenever a game finishes, the team that is put in as parameter is the winning team
    abstract fun finishGame(winningTeam : Team)

    abstract fun drawGame()

    // this method will be called whenever the time is up, you can decide what to do with this game whatever you want
    // you can find a winner and call finishGame on that, or you can decide that it is always draw,
    // or you do something else...
    open fun timeUpEnding(){
        drawGame()
    }

    // this method clear the game, if this is called before the game is finished, the game will not have a winner :(
    open fun clearGame() : Boolean{
        if(gameTeams == null) return false
        gameTeams!!.forEachObject { cardManager -> cardManager.clear() }
        gameTeams!!.clear()
        gameTeams = null
        return true
    }

    open fun debugStatus(commander: Player) {}

    open fun requestTasks(amount: Int, team: Team) : Array<ITask>? {
        return TaskTussleSystem.generateTasks(amount, team, gameTeams!!, emptyList())
    }


    protected fun addOvertimeSetting(overtimeOptions : Array<String>){
        atOvertimeSetting = overtimeOptions[0]

        val drawInitiallyIcon = UniqueItemStack(
            Material.STICK, "",
            ("${ChatColor.GRAY}If there is no game time left " +
                    "(aka, time hits 0 minutes). This does not effect games " +
                    "played where the time is disabled.").toLore(32) )
            .updateEnchanted(true)

        var currentOptionIndex = 0
        settingsInventory.addSetting(drawInitiallyIcon,{
            drawInitiallyIcon
                .updateTitle("${TaskTussleSettings.settingColor}Overdue:${ChatColor.YELLOW} $atOvertimeSetting")
                .pushUpdates()
        }, {_,_ ->
            currentOptionIndex++
            currentOptionIndex %= overtimeOptions.size
            atOvertimeSetting = overtimeOptions[currentOptionIndex]
        }, {_,_ ->
            currentOptionIndex--
            if(currentOptionIndex < 0) currentOptionIndex = 0
            atOvertimeSetting = overtimeOptions[currentOptionIndex]
        })
    }

    protected fun addSkipTokenSetting(){
        val tokenIcon = UniqueItemStack(
            Material.RED_CANDLE, "",
            listOf(
                "${ChatColor.DARK_GRAY}L Click: ${ChatColor.GRAY}Increase amount",
                "${ChatColor.DARK_GRAY}R Click: ${ChatColor.GRAY}Decrease amount"
            ) +
                    "${ChatColor.GRAY}Skip tokens allow teams to skip a task by failing it.".toLore(32) )

        settingsInventory.addSetting(tokenIcon,{
            tokenIcon
                .updateTitle("${TaskTussleSettings.settingColor}Skip token:${ChatColor.YELLOW} ${TaskTussleSystem.skipTokens}")
                .updateCount(max(1, TaskTussleSystem.skipTokens))
                .updateEnchanted(TaskTussleSystem.skipTokens > 0)
                .pushUpdates()
        },
            {_,_ -> TaskTussleSystem.skipTokens = min(100, TaskTussleSystem.skipTokens+1) },
            {_,_ -> TaskTussleSystem.skipTokens = max(0, TaskTussleSystem.skipTokens -1) }
        )
    }
    protected fun addSucceedTokenSetting(){
        val tokenIcon = UniqueItemStack(
            Material.LIME_CANDLE, "",
            listOf(
                "${ChatColor.DARK_GRAY}L Click: ${ChatColor.GRAY}Increase amount",
                "${ChatColor.DARK_GRAY}R Click: ${ChatColor.GRAY}Decrease amount"
            ) +
                    "${ChatColor.GRAY}Succeed tokens allow teams to complete a task instantly".toLore(32) )

        settingsInventory.addSetting(tokenIcon,{
            tokenIcon
                .updateTitle("${TaskTussleSettings.settingColor}Succeed token:${ChatColor.YELLOW} ${TaskTussleSystem.succeedTokens}")
                .updateCount(max(1, TaskTussleSystem.succeedTokens))
                .updateEnchanted(TaskTussleSystem.succeedTokens > 0)
                .pushUpdates()
        },
            {_,_ -> TaskTussleSystem.succeedTokens = min(100, TaskTussleSystem.succeedTokens+1) },
            {_,_ -> TaskTussleSystem.succeedTokens = max(0, TaskTussleSystem.succeedTokens -1) }
        )
    }
}
