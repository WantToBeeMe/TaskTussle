package me.wanttobee.tasktussle.tasks.obtainTask

import me.wanttobee.everythingitems.ItemUtil.getRealName
import me.wanttobee.everythingitems.ItemUtil.getSubTitle
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.TaskIcon
import me.wanttobee.tasktussle.generic.tasks.TaskEventsListener
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent

class ObtainTask(val itemToObtain : Material, associatedTeam : Team) : ITask(associatedTeam){
    override val icon: TaskIcon = TaskIcon(itemToObtain, itemToObtain.getRealName(),ObtainTaskManager.taskName, {"0/1"} ,
        if(itemToObtain.getSubTitle() == null) listOf("obtain this item")
        else listOf("obtain this item","${ChatColor.GRAY}${itemToObtain.getSubTitle()}") )

    // handIn is the option if it should take 1 from the item stack or if it should leave the amount
    // This task will be generated with this value taken from ObtainTaskManager.handInItem,
    // but it will also be saved here for when ObtainTaskManager changes, to make sure this task doesn't break
    private var handIn = false

    private val pickupEvent : (EntityPickupItemEvent) -> Unit = event@{ event ->
        if(handIn) return@event
        val player = event.entity as? Player ?: return@event
        val itemType = event.item.itemStack.type
        if(associatedTeam.containsMember(player)){
            if(itemType == itemToObtain){
                this.setCompleted()
            }
        }
    }

    private val inventoryClick : (InventoryClickEvent) -> Unit = event@{ event ->
        val player = event.whoClicked as? Player ?: return@event
        val cursorItem = event.cursor ?: return@event
        val cardItem = event.currentItem ?: return@event
        if(TaskTussleSystem.clickItem.isThisItem(cardItem)
            && cursorItem.type == itemToObtain
            && associatedTeam.containsMember(player) ){
            this.setCompleted()
            if(handIn) cursorItem.amount -= 1
        }
    }

    override fun enable() {
        if(!ObtainTaskManager.handInItem)
            TaskEventsListener.entityPickupItemEvent.add(pickupEvent)
        // only when we are allowed to keep the item do we allow for pickups,
        // because we cant remove something from a stack in the pickup event for some reason

        TaskEventsListener.inventoryClickObservers.add(inventoryClick)
        handIn = ObtainTaskManager.handInItem
    }
    override fun disable() {
        TaskEventsListener.entityPickupItemEvent.remove(pickupEvent)
        TaskEventsListener.inventoryClickObservers.remove(inventoryClick)
    }

    override fun getSuccessMessage(hideDetails: Boolean): String {
        return if(hideDetails)
            "${associatedTeam.getDisplayName()}${ChatColor.RESET} got a task"
        else "${associatedTeam.getDisplayName()}${ChatColor.RESET} got an obtain task ${ChatColor.GRAY}(${itemToObtain.getRealName()})"
    }

    override fun clone(otherTeam : Team): ObtainTask {
        return ObtainTask(itemToObtain,otherTeam)
    }
}
