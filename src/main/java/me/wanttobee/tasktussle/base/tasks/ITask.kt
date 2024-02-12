package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.cards.ITTCard
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

//the stateCode's in order are
// 0 -> active
// 1 -> Completed
// 2 -> CompletedBy ??
// 3 -> Hidden
// 4 -> Locked
// 5 -> Failed
abstract class ITask(val associatedTeam : Team?, val associatedSet : TeamSet<*>) {
    abstract val icon : TaskIcon
    var assosiatedCard : ITTCard? = null
        private set
    var stateCode : TaskState = TaskState.HIDDEN
        private set
    var completerTeam : Team? = null
        private set
    var contributors : MutableSet<String> = mutableSetOf()
        private set

    // this is the inventory for the options for this task
    // its only something when it is being used, otherwise its null
    private var taskOptions : TaskOptions? = null

    fun addContributor(playerName: String) : Boolean{
        return contributors.add(playerName)
    }

    // we need to pass in the TaskCardManager in able to know to what manager this task should be subscribed
    fun setActive(callBackCard : ITTCard){
        completerTeam = null
        stateCode = TaskState.ACTIVE
        icon.setState(stateCode)
        internalEnable(callBackCard)
    }

    fun setCompleted(completer: Player) {
        addContributor(completer.name)
        return setCompleted(associatedSet.getTeam(completer)!!, false)
    }
    fun setCompleted(completerTeam: Team, wholeTeamContributed: Boolean = true){
        this.completerTeam = completerTeam
        if(wholeTeamContributed)
            completerTeam.forEachMember { p -> addContributor(p.name) }
        completerTeam.forEachMember { p ->
            p.playSound(p.location, Sound.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.MASTER, 0.3f, 1f)
        }
        // when the associated team is null, that means all teams can complete it, and we have to set the state to show
        // that not besides that it is completed, that also the team who completed it matters
        stateCode = if(associatedTeam != null) TaskState.COMPLETED else TaskState.COMPLETED_BY
        icon.setState(stateCode, completerTeam, contributors)

        if(TaskTussleSystem.cardVisibility != "visible" && associatedTeam != null){
            associatedSet.forEachTeam{ team ->
                team.forEachMember { p ->
                    if(team == completerTeam)
                        p.sendMessage(getSuccessMessage(completerTeam))
                    else if(TaskTussleSystem.cardVisibility != "hidden"){
                        p.sendMessage("${completerTeam.getDisplayName()}${ChatColor.RESET} got a task")
                    }

                } }
        }
        else{
            associatedSet.broadcast(getSuccessMessage(completerTeam))
        }
        internalDisable()
    }
    fun setHidden(){
        completerTeam = null
        stateCode = TaskState.HIDDEN
        icon.setState(stateCode)
        internalDisable()
    }
    fun setLocked(){
        completerTeam = null
        stateCode = TaskState.LOCKED
        icon.setState(stateCode)
        internalDisable()
    }
    fun setFailed(){
        completerTeam = null
        stateCode = TaskState.FAILED
        icon.setState(stateCode)
        internalDisable()
    }

    // this method will be called only from this abstract class, it is to make sure the task will not work anymore
    // the public implementation is just disable()
    private fun internalDisable(){
        if(assosiatedCard == null)
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITask) ERROR: Cant do task-callback for disable call (no card manager assigned)")
        else{
            assosiatedCard!!.onTaskDisabled(this)
            TaskEventsListener.taskObserver.remove(this)
            taskOptions?.clear()
            taskOptions = null
            disable()
        }

    }

    // this method will be called only from this abstract class, it is to make sure the task will work
    // the public implementation is just enable()
    private fun internalEnable(callBackCard : ITTCard){
        this.assosiatedCard = callBackCard
        TaskEventsListener.taskObserver.add(this)
        enable()
    }

    fun isPlayerAllowed(potentialCompleter : Player) : Boolean{
        return associatedTeam?.containsMember(potentialCompleter) ?: associatedSet.containsPlayer(potentialCompleter)
    }

    protected abstract fun enable()
    protected abstract fun disable()

    abstract fun getSuccessMessage(completerTeam: Team) : String
    abstract fun clone(otherTeam : Team) : ITask

    // this method will run when you left or right-click on the tasks icon AND the player doing so is allowed to complete the task.
    // if a task is tied to a team. this player belongs to that team, or if more teams are allowed to complete the task,
    // than the player belongs to one of those teams
    open fun onLeftClickIcon(player : Player, shift: Boolean, event: InventoryClickEvent){/* nothing by default */ }
    open fun onRightClickIcon(player : Player, shift: Boolean, event: InventoryClickEvent){
        if((assosiatedCard?.skipTokensMax ?: 0) == 0 &&
            (assosiatedCard?.successTokensMax ?: 0) == 0) {
            // when we didn't have any tokens to begin with, we can just ignore this call and not open the inventory at all, there is no use anyway
            return
        }

        if(taskOptions == null) taskOptions = TaskOptions(this)
        taskOptions!!.open(player)
    }
}
