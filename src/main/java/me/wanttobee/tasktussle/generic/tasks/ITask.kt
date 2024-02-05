package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.cards.ITTCard
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player

//the stateCode's in order are
// 0 -> active
// 1 -> Completed
// 2 -> CompletedBy ??
// 3 -> Hidden
// 4 -> Locked
// 5 -> Failed
abstract class ITask(val associatedTeam : Team?, val associatedSet : TeamSet<*>) {
    abstract val icon : TaskIcon
    private var callBackCard : ITTCard? = null
    var stateCode : TaskState = TaskState.HIDDEN
        private set
    var completerTeam : Team? = null
        private set
    var contributors : Array<Player>? = null
        private set

    // we need to pass in the TaskCardManager in able to know to what manager this task should be subscribed
    fun setActive(callBackCard : ITTCard){
        completerTeam = null
        contributors = null
        stateCode = TaskState.ACTIVE
        icon.setState(stateCode)
        internalEnable(callBackCard)
    }

    fun setCompleted(completer: Player){
        return setCompleted(associatedSet.getTeam(completer)!!, arrayOf(completer))
    }
    fun setCompleted(completer: Player, contributors: Array<Player>) {
        return setCompleted(associatedSet.getTeam(completer)!!, contributors)
    }
    fun setCompleted(completerTeam: Team, contributors: Array<Player>){
        this.completerTeam = completerTeam
        this.contributors = contributors
        completerTeam.forEachMember { p ->
            p.playSound(p.location, Sound.BLOCK_AMETHYST_BLOCK_HIT, SoundCategory.MASTER, 0.3f, 1f)
        }
        // when the associated team is null, that means all teams can complete it, and we have to set the state to show
        // that not besides that it is completed, that also the team who completed it matters
        stateCode = if(associatedTeam != null) TaskState.COMPLETED else TaskState.COMPLETED_BY
        icon.setState(stateCode, completerTeam, contributors)
        internalDisable()

        if(TaskTussleSystem.hideCard && associatedTeam != null){
            associatedSet.forEachTeam{ team ->
                team.forEachMember { p ->
                    if(team == completerTeam)
                        p.sendMessage(getSuccessMessage(completerTeam))
                    else
                        p.sendMessage("${completerTeam.getDisplayName()}${ChatColor.RESET} got a task")
                } }
        }
        else{
            associatedSet.broadcast(getSuccessMessage(completerTeam))
        }
    }
    fun setHidden(){
        completerTeam = null
        contributors = null
        stateCode = TaskState.HIDDEN
        icon.setState(stateCode)
        internalDisable()
    }
    fun setLocked(){
        completerTeam = null
        contributors = null
        stateCode = TaskState.LOCKED
        icon.setState(stateCode)
        internalDisable()
    }
    fun setFailed(){
        completerTeam = null
        contributors = null
        stateCode = TaskState.FAILED
        icon.setState(stateCode)
        internalDisable()
    }

    // this method will be called only from this abstract class, it is to make sure the task will not work anymore
    // the public implementation is just disable()
    private fun internalDisable(){
        if(callBackCard == null)
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITask) ERROR: Cant do task-callback for disable call (no card manager assigned)")
        else{
            callBackCard!!.onTaskDisabled(this)
            disable()
        }

    }

    // this method will be called only from this abstract class, it is to make sure the task will work
    // the public implementation is just enable()
    private fun internalEnable(callBackCard : ITTCard){
        this.callBackCard = callBackCard
        enable()
    }

    protected fun isPlayerAllowed(potentialCompleter : Player) : Boolean{
        return associatedTeam?.containsMember(potentialCompleter) ?: associatedSet.containsPlayer(potentialCompleter)
    }

    protected abstract fun enable()
    protected abstract fun disable()

    abstract fun getSuccessMessage(completerTeam: Team) : String
    abstract fun clone(otherTeam : Team) : ITask
}
