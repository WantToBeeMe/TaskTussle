package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.games.ITTCardLogic
import me.wanttobee.tasktussle.base.games.ITTGameTeam
import me.wanttobee.tasktussle.teams.Team
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
abstract class ITask() {
    abstract val icon : TaskIcon
    var ownerCard : ITTCardLogic<*>? = null
        private set
    var stateCode : TaskState = TaskState.HIDDEN
        private set
    var completerTeam : ITTGameTeam? = null
        private set
    var contributors : MutableSet<String> = mutableSetOf()
        private set

    // this is the inventory for the options for this task
    // its only something when it is being used, otherwise its null
    private var taskOptions : TaskOptions? = null

    //region ownership / contributors

    fun setOwnership(owner: ITTCardLogic<*>){
        if (ownerCard != null){
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITask) ERROR: Cant set task ownership, already has an owner")
            return
        }

        this.ownerCard = owner
    }

    fun addContributor(playerName: String) : Boolean{
        return contributors.add(playerName)
    }

    fun isPlayerAllowed(potentialCompleter : Player) : Boolean{
        for (asTeam in ownerCard!!.associatedGameTeams){
            if(asTeam.containsMember(potentialCompleter))
                return true
        }
        return false
        //return associatedCard?.containsMember(potentialCompleter) ?: associatedSet.containsPlayer(potentialCompleter)
    }

    //endregion
    //region state management

    // we need to pass in the TaskCardManager in able to know to what manager this task should be subscribed
    fun setActive(){
        revokeTaskCompletion()
        stateCode = TaskState.ACTIVE
        icon.setState(stateCode)
        internalEnable()
    }

    fun setCompleted(completer: Player) {
        addContributor(completer.name)
        val a = ownerCard!!.associatedSet.getObject(completer)!!
        return setCompleted(a, false)
    }

    fun setCompleted(completerTeam: ITTGameTeam, wholeTeamContributed: Boolean = true){
        this.completerTeam = completerTeam
        if(wholeTeamContributed)
            completerTeam.forEachMember { p -> addContributor(p.name) }

        completerTeam.forEachMember { p ->
            p.playSound(p.location, Sound.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.MASTER, 0.3f, 1f)
        }
        // when the associated team is null, that means all teams can complete it, and we have to set the state to show
        // that not besides that it is completed, that also the team who completed it matters
        stateCode = TaskState.COMPLETED // if(associatedTeam != null) TaskState.COMPLETED else TaskState.COMPLETED_BY
        icon.setState(stateCode, completerTeam.associatedTeam, contributors)
        completerTeam.onTaskCompleted(this)
        internalDisable()
    }
    fun setHidden(){
        revokeTaskCompletion()
        stateCode = TaskState.HIDDEN
        icon.setState(stateCode)
        internalDisable()
    }
    fun setLocked(){
        revokeTaskCompletion()
        stateCode = TaskState.LOCKED
        icon.setState(stateCode)
        internalDisable()
    }
    fun setFailed(){
        revokeTaskCompletion()
        stateCode = TaskState.FAILED
        icon.setState(stateCode)
        internalDisable()
    }

    // this method will be called only from this abstract class, it is to make sure the task will not work anymore
    // the public implementation is just disable()
    private fun internalDisable(){
        if(ownerCard == null){
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITask) ERROR: Cant do task-callback for disable call (no owner card assigned)")
            return
        }

        ownerCard!!.onTaskDisabled(this)
        TaskEventsListener.taskObserver.remove(this)
        taskOptions?.clear()
        taskOptions = null
        disable()
    }

    // this method will be called only from this abstract class, it is to make sure the task will work
    // the public implementation is just enable()
    private fun internalEnable(){
        if(ownerCard == null){
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITask) ERROR: Cant do task-callback for enable call (no owner card assigned)")
            return
        }

        ownerCard!!.onTaskEnabled(this)
        TaskEventsListener.taskObserver.add(this)
        enable()
    }

    private fun revokeTaskCompletion(){
        // If it had a completer team, it now loses it and notifies the team
        completerTeam?.onTaskRevoked(this)
        completerTeam = null
        contributors.clear()
    }

    protected abstract fun enable()
    protected abstract fun disable()
    //endregion

    abstract fun getSuccessMessage(completerTeam: Team) : String
    abstract fun clone() : ITask

    // this method will run when you left or right-click on the tasks icon AND the player doing so is allowed to complete the task.
    // if a task is tied to a team. this player belongs to that team, or if more teams are allowed to complete the task,
    // than the player belongs to one of those teams
    open fun onLeftClickIcon(player : Player, shift: Boolean, event: InventoryClickEvent){ /* nothing by default */ }
    open fun onRightClickIcon(player : Player, shift: Boolean, event: InventoryClickEvent){
        if((ownerCard?.skipTokensMax ?: 0) == 0 &&
            (ownerCard?.successTokensMax ?: 0) == 0) {
            // when we didn't have any tokens to begin with, we can just ignore this call and not open the inventory at all, there is no use anyway
            return
        }

        if(taskOptions == null) taskOptions = TaskOptions(this)
        taskOptions!!.open(player)
    }
}
