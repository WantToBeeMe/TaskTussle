package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.cards.ITTCard
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupItemEvent

//the stateCode's in order are
// 0 -> active
// 1 -> Completed
// 2 -> CompletedBy ??
// 3 -> Hidden
// 4 -> Locked
// 5 -> Failed
abstract class ITask(val associatedTeam : Team) {
    abstract val icon : TaskIcon
    private var callBackCard : ITTCard? = null
    var stateCode : TaskState = TaskState.HIDDEN
        private set

    // we need to pass in the TaskCardManager in able to know to what manager this task should be subscribed
    fun setActive(callBackCard : ITTCard){
        stateCode = TaskState.ACTIVE
        icon.setState(stateCode)
        internalEnable(callBackCard)
    }
    fun setCompleted(){
        stateCode = TaskState.COMPLETED
        icon.setState(stateCode)
        internalDisable()
    }

    fun setCompletedBy(teamColor : ChatColor, teamTitle : String){
        stateCode = TaskState.COMPLETED_BY
        icon.setState(stateCode,teamColor,teamTitle)
        internalDisable()
    }
    fun setHidden(){
        stateCode = TaskState.HIDDEN
        icon.setState(stateCode)
        internalDisable()
    }
    fun setLocked(){
        stateCode = TaskState.LOCKED
        icon.setState(stateCode)
        internalDisable()
    }
    fun setFailed(){
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

    protected abstract fun disable()
    protected abstract fun enable()


    abstract fun getSuccessMessage(hideDetails : Boolean) : String


    abstract fun clone(otherTeam : Team) : ITask

    //all the different possible listeners
    // open fun checkTask(){} //for the tick one
    open fun checkTask(event : EntityPickupItemEvent){}
    open fun checkTask(event : PlayerInteractEvent){}
    open fun checkTask(event : BlockBreakEvent){}
    open fun checkTask(event : BlockPlaceEvent){}
    open fun checkTask(event : EntityDeathEvent){}
    open fun checkTask(event : PlayerDeathEvent){}
    open fun checkTask(event : InventoryClickEvent){}
}
