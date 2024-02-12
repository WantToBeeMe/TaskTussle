package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.teams.TeamSystem
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent

object TaskEventsListener : Listener {
    // when a task gets enabled they get added to this list, and on disable they get removed
    // this is to check if someone on the team left-clicked, or right-clicked this task
    // If that is the case we call the left or right click event, we can use the build in left and right click callback
    //      the way that the icon is currently made, that requires a lot more effort than just building a new detection,
    //      so that's what we are doing here, might be changed later
    val taskObserver : MutableSet<ITask> = mutableSetOf()

    // all the other observers that may be used
    val entityPickupItemObservers : MutableSet<(EntityPickupItemEvent) -> Unit> = mutableSetOf()
    val inventoryClickObservers : MutableSet<(InventoryClickEvent) -> Unit> = mutableSetOf()
    val advancementObservers : MutableSet<(PlayerAdvancementDoneEvent) -> Unit> = mutableSetOf()

    // we clone the list (but in a typed array because that's way faster) so that we can modify the original list
    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: EntityPickupItemEvent) {
        if(TaskTussleSystem.completeTasksLocked) return
        for(eventAction in entityPickupItemObservers.toTypedArray())
            eventAction.invoke(event)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: InventoryClickEvent){
        if(TaskTussleSystem.completeTasksLocked) return

        val cardItem = event.currentItem
        val player = event.whoClicked as? Player
        //clicking on the task icon, instead of something else
        if(player != null && cardItem != null ){
            val task = taskObserver.find { it.icon.isThisItem(cardItem) }
            if(task != null){
                if(task.isPlayerAllowed(player)){
                    if (event.click.isLeftClick)
                        task.onLeftClickIcon(player, event.isShiftClick, event)
                    else if (event.click.isRightClick)
                        task.onRightClickIcon(player, event.isShiftClick, event)
                }

                // if it's a click on one of the icons, we can ignore the events from this,
                // and thus we return early
                return
            }
        }

        for(eventAction in inventoryClickObservers.toTypedArray())
            eventAction.invoke(event)
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: PlayerAdvancementDoneEvent){
        if(TaskTussleSystem.completeTasksLocked) return
        for(eventAction in advancementObservers.toTypedArray())
            eventAction.invoke(event)
    }

    fun debugStatus(commander: Player){
        commander.sendMessage("${TaskTussleSystem.title} ${ChatColor.YELLOW}active tasks:")
        val activeTasks = taskObserver.size
        val eventsCount = entityPickupItemObservers.size +
                inventoryClickObservers.size +
                advancementObservers.size

        if(activeTasks == 0 && eventsCount == 0){
            commander.sendMessage("${ChatColor.GREEN}there are no tasks active")
            return
        }
        commander.sendMessage("${ChatColor.WHITE}There are $activeTasks tasks active ${ChatColor.GREEN}(and use up in total $eventsCount events)")
    }

    fun clear(){
        taskObserver.clear()
        entityPickupItemObservers.clear()
        inventoryClickObservers.clear()
        advancementObservers.clear()
    }
}
