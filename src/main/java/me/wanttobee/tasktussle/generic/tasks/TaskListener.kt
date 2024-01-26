package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPickupItemEvent

object TaskListener : Listener {
    // val tickObservers : MutableList<ITask> = mutableListOf()
    val playerInteractObservers : MutableList<ITask> = mutableListOf()
    val playerPickupItemObservers : MutableList<ITask> = mutableListOf()
    val blockBreakObservers : MutableList<ITask> = mutableListOf()
    val blockPlaceObservers : MutableList<ITask> = mutableListOf()
    val entityDeathObservers : MutableList<ITask> = mutableListOf()
    val playerDeathObservers : MutableList<ITask> = mutableListOf()
    val inventoryClickObservers : MutableList<ITask> = mutableListOf()

    // as you can see in all the event handlers, I am iterating in reverse over the list
    // there are a couple of reasons for this. In the checkTask method there is a possibility
    // that that task will remove itself from the list,
    // 1. doing foreach would not work (modifying an iterating list error)
    // 2. iterating over each index would also not work (index out of bound)
    // 3. But iterating over each index in reverse would work, if you would remove something then, it would still mess up the order/length for that particular loop
    //    but that's no problem because then we are already past that bit
    //    keep in mind, if we eventually decide to also add in the checkTask() method, then this would be a problem again
    // 4. cloning the list before iterating, This will always work no matter the modification you do, however, this is also the least efficient,
    //    so we are not going to as long as we don't have to

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: PlayerInteractEvent) {
        if(TaskTussleSystem.completeTasksLocked) return
        for(i in playerInteractObservers.indices.reversed()){
            playerInteractObservers[i].checkTask(event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: EntityPickupItemEvent) {
        if(TaskTussleSystem.completeTasksLocked) return
        for(i in playerPickupItemObservers.indices.reversed()){
            playerPickupItemObservers[i].checkTask(event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: BlockBreakEvent) {
        if(TaskTussleSystem.completeTasksLocked) return
        for(i in blockBreakObservers.indices.reversed()){
            blockBreakObservers[i].checkTask(event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: BlockPlaceEvent) {
        if(TaskTussleSystem.completeTasksLocked) return
        for(i in blockPlaceObservers.indices.reversed()){
            blockPlaceObservers[i].checkTask(event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: EntityDeathEvent) {
        if(TaskTussleSystem.completeTasksLocked) return
        for(i in entityDeathObservers.indices.reversed()){
            entityDeathObservers[i].checkTask(event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: PlayerDeathEvent) {
        if(TaskTussleSystem.completeTasksLocked) return
        for(i in playerDeathObservers.indices.reversed()){
            playerDeathObservers[i].checkTask(event)
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    fun taskSystemEventHandler(event: InventoryClickEvent){
        if(TaskTussleSystem.completeTasksLocked) return
        for(i in inventoryClickObservers.indices.reversed()){
            inventoryClickObservers[i].checkTask(event)
        }
    }
}
