package me.wanttobee.tasktussle.tasks.obtainTask

import me.wanttobee.everythingitems.ItemUtil.getRealName
import me.wanttobee.everythingitems.ItemUtil.getSubTitle
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.TaskIcon
import me.wanttobee.tasktussle.generic.tasks.TaskListener
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerPickupItemEvent


class ObtainTask(val itemToObtain : Material, associatedTeam : Team) : ITask(associatedTeam)  {
    override val icon: TaskIcon = TaskIcon(itemToObtain, itemToObtain.getRealName(),"Obtain Item", {"0/1"} ,
        if(itemToObtain.getSubTitle() == null) listOf("obtain this item")
        else listOf("obtain this item","${ChatColor.GRAY}${itemToObtain.getSubTitle()}") )

    // handIn is the option if it should take 1 from the item stack or if it should leave the amount
    // This task will be generated with this value taken from ObtainTaskManager.handInItem,
    // but it will also be saved here for when ObtainTaskManager changes, to make sure this task doesn't break
    private var handIn = false

    override fun enable() {
        if(!ObtainTaskManager.handInItem)
            TaskListener.playerPickupItemObservers.add(this)
        // only when we are allowed to keep the item do we allow for pickups,
        // because we cant remove something from a stack in the pickup event for some reason

        TaskListener.inventoryClickObservers.add(this)
        handIn = ObtainTaskManager.handInItem
    }
    override fun disable() {
        TaskListener.playerPickupItemObservers.remove(this)
        TaskListener.inventoryClickObservers.remove(this)
    }

    override fun checkTask(event: PlayerPickupItemEvent) : (() -> Unit)? {
        if(handIn) return null
        if(associatedTeam.containsMember(event.player)){
            if(event.item.itemStack.type == itemToObtain)
                return {this.setCompleted()}
        }
        return null
    }

    override fun checkTask(event: InventoryClickEvent): (() -> Unit)? {
        val player = event.whoClicked as? Player ?: return null
        val cursorItem = event.cursor ?: return null
        val cardItem = event.currentItem ?: return null
        if(TaskTussleSystem.clickItem.isThisItem(cardItem)
            && cursorItem.type == itemToObtain
            && associatedTeam.containsMember(player) ){
            return {
                this.setCompleted()
                if(handIn) cursorItem.amount -= 1
            }
        }
        return null
    }

    override fun clone(otherTeam : Team): ObtainTask {
        return ObtainTask(itemToObtain,otherTeam)
    }

}