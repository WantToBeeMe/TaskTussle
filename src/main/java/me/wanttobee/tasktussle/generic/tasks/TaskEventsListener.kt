package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent

object TaskEventsListener : Listener {
    val entityPickupItemObservers : MutableList<(EntityPickupItemEvent) -> Unit> = mutableListOf()
    val inventoryClickObservers : MutableList<(InventoryClickEvent) -> Unit> = mutableListOf()
    val advancementObservers : MutableList<(PlayerAdvancementDoneEvent) -> Unit> = mutableListOf()

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
        for(eventAction in inventoryClickObservers.toTypedArray()){
            eventAction.invoke(event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: PlayerAdvancementDoneEvent){
        if(TaskTussleSystem.completeTasksLocked) return
        for(eventAction in advancementObservers.toTypedArray()){
            eventAction.invoke(event)
        }
    }
}
