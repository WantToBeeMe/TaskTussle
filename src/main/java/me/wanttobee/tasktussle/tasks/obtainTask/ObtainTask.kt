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
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import kotlin.math.min

class ObtainTask(val itemToObtain : Material, associatedTeam : Team,private var obtainAmount: Int = 1) : ITask(associatedTeam){
    // handIn is the option if it should take 1 from the item stack or if it should leave the amount
    // This task will be generated with this value taken from ObtainTaskManager.handInItem,
    // but it will also be saved here for when ObtainTaskManager changes, to make sure this task doesn't break
    private var handIn = false

    private var alreadyObtained = 0
    override val icon: TaskIcon = TaskIcon(itemToObtain, itemToObtain.getRealName(),ObtainTaskManager.taskName, {"$alreadyObtained/${this.obtainAmount}"} ,
        if(itemToObtain.getSubTitle() == null) listOf("obtain this item")
        else listOf("obtain this item","${ChatColor.GRAY}${itemToObtain.getSubTitle()}") )

    init{
        // making sure that it is not impossible, 100 shovels is impossible because you don't have 100 inventory slots
        this.obtainAmount = min(obtainAmount, itemToObtain.maxStackSize * (9*3))
        icon.refreshProgression()
    }

    private val pickupEvent : (EntityPickupItemEvent) -> Unit = event@{ event ->
        if(handIn) return@event
        // this event will not be triggered if handIn is set to true, that means you can assume that
        // you can keep the item here. however, that also means that you have to do all the items at once
        val player = event.entity as? Player ?: return@event
        if(!associatedTeam.containsMember(player)) return@event
        val itemType = event.item.itemStack.type
        if(itemType == itemToObtain){
            // if the amount was 1 (which is a lot of the times) we can skip the whole calculating bit
            if(obtainAmount == 1){
                this.setCompleted()
                return@event
            }

            // we also check how much the player has of this item,
            // if someone needs to get 100 of this item, it's pretty impossible to do that with 1 pickup
            val alreadyOwns = player.inventory.sumOf { item ->
                if(item?.type != itemToObtain) 0
                else item.amount
            }
            val stillNeedToObtain = obtainAmount - alreadyObtained - alreadyOwns
            if(event.item.itemStack.amount >= stillNeedToObtain)
                this.setCompleted()
        }
    }

    private val inventoryClick : (InventoryClickEvent) -> Unit = event@{ event ->
        val player = event.whoClicked as? Player ?: return@event
        if(!associatedTeam.containsMember(player)) return@event
        val cardItem = event.currentItem ?: return@event
        val cursorItem = event.cursor

        // When amount = 1
        // we check this first, even though it would also work in the other one, this is of-course the faster approach
        if(obtainAmount == 1){
            if(TaskTussleSystem.clickItem.isThisItem(cardItem)
                && cursorItem?.type == itemToObtain){
                this.setCompleted()
                if(handIn) cursorItem.amount -= 1
            }
            else if(icon.isThisItem(cardItem)){
                if(cursorItem?.type == itemToObtain){
                    this.setCompleted()
                    if(handIn) cursorItem.amount -= 1
                    return@event
                }
                val checkItem = player.inventory.find { item -> item?.type == itemToObtain }
                if(checkItem != null){
                    this.setCompleted()
                    if(handIn) checkItem.amount -= 1
                }
            }
            return@event
        }

        // When amount is bigger than 1, and you have to hand it in
        if(handIn){
            // when we click with an item in our hand we only want to remove it from the stack that we have in our hand
            if((TaskTussleSystem.clickItem.isThisItem(cardItem) || icon.isThisItem(cardItem))
                && cursorItem?.type == itemToObtain){
                val obtainingNow = min(cursorItem.amount, obtainAmount - alreadyObtained)
                cursorItem.amount -= obtainingNow
                alreadyObtained += obtainingNow
                icon.refreshProgression()
                if(alreadyObtained == obtainAmount)
                    this.setCompleted()
                else
                    player.playSound(player.location, Sound.ENTITY_LLAMA_CHEST, SoundCategory.MASTER, 0.2f, 1f)
            }
        }

        // When amount is bigger than 1, and you get to keep everything
        else{
            var currentlyObtained = 0
            if((TaskTussleSystem.clickItem.isThisItem(cardItem) || icon.isThisItem(cardItem))
                && cursorItem?.type == itemToObtain){
                currentlyObtained = cursorItem.amount
            }
            // either you click with this item in hand, or you click on the task icon itself
            if(currentlyObtained > 0 || icon.isThisItem(cardItem)){
                player.sendMessage("check $currentlyObtained")
                if(currentlyObtained >= obtainAmount - alreadyObtained){
                    // if this stack turns out to be enough already ,we don't have to loop through the whole inventory
                    this.setCompleted()
                    return@event
                }
                val obtainItems = player.inventory.filter { itemStack -> itemStack?.type == itemToObtain }
                currentlyObtained += obtainItems.sumOf { itemStack -> itemStack.amount }
                player.sendMessage("check $currentlyObtained")
                if(currentlyObtained >= obtainAmount - alreadyObtained)
                    this.setCompleted()
            }
        }
    }

    override fun enable() {
        handIn = ObtainTaskManager.handInItem
        if(!handIn)
            TaskEventsListener.entityPickupItemEvent.add(pickupEvent)
        // only when we are allowed to keep the item do we allow for pickups,
        // because we cant remove something from a stack in the pickup event for some reason
        TaskTussleSystem.log("enabling obtain task ${itemToObtain.name}")
        TaskEventsListener.inventoryClickObservers.add(inventoryClick)
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
        return ObtainTask(itemToObtain, otherTeam, obtainAmount)
    }
}
