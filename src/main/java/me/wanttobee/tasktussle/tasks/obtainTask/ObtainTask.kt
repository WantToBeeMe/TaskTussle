package me.wanttobee.tasktussle.tasks.obtainTask

import me.wanttobee.everythingitems.ItemUtil.getRealName
import me.wanttobee.everythingitems.ItemUtil.getSubTitle
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskIcon
import me.wanttobee.tasktussle.base.tasks.TaskEventsListener
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class ObtainTask(associatedTeam : Team?, associatedSet: TeamSet<*>, val itemToObtain : Material, private var obtainAmount: Int = 1)
    : ITask(associatedTeam, associatedSet){
    override val icon: TaskIcon = TaskIcon(itemToObtain,
        itemToObtain.getRealName(),ObtainTaskManager.taskName,
        {"$alreadyObtained/${this.obtainAmount}"} ,
        if(itemToObtain.getSubTitle() == null) "obtain this item"
        else "obtain this item. ${ChatColor.GRAY}${itemToObtain.getSubTitle()}" )

    override fun getSuccessMessage(completerTeam: Team): String {
        return "${completerTeam.getDisplayName()}${ChatColor.RESET} got an obtain task ${ChatColor.GRAY}(${itemToObtain.getRealName()})"
    }

    // handIn is the option if it should take 1 from the item stack or if it should leave the amount
    // This task will be generated with this value taken from ObtainTaskManager.handInItem,
    // but it will also be saved here for when ObtainTaskManager changes, to make sure this task doesn't break
    private var handIn = false
    private var alreadyObtained = 0

    init{
        // making sure that it is not impossible, 100 shovels is impossible because you don't have 100 inventory slots
        this.obtainAmount = min(obtainAmount, itemToObtain.maxStackSize * (9*3))
        icon.refreshProgression()
    }

    override fun onLeftClickIcon(player: Player, shift: Boolean, event: InventoryClickEvent) {
        val cursorItem = event.cursor
        // When amount = 1
        // we check this first, even though it would also work in the other one, this is of-course the faster approach
        if(obtainAmount == 1){
            val checkItem = if(cursorItem?.type == itemToObtain)
                cursorItem
            else player.inventory.find { item -> item?.type == itemToObtain }
            if(checkItem != null){
                this.setCompleted(player)
                if(handIn) checkItem.amount -= 1
            }
            return
        }

        if(handIn){
            onHandInMultiple(player,cursorItem)
            return
        }
        onKeepMultiple(player, cursorItem, true)
    }

    private fun pickupEvent(event: EntityPickupItemEvent ){
        if(handIn) return
        // this event will not be triggered if handIn is set to true, that means you can assume that
        // you can keep the item here. however, that also means that you have to do all the items at once
        val player = event.entity as? Player ?: return
        if(!isPlayerAllowed(player)) return
        val itemType = event.item.itemStack.type
        if(itemType == itemToObtain){
            // if the amount was 1 (which is a lot of the times) we can skip the whole calculating bit
            if(obtainAmount == 1){
                this.setCompleted(player)
                return
            }

            // we also check how much the player has of this item,
            // if someone needs to get 100 of this item, it's pretty impossible to do that with 1 pickup
            val alreadyOwns = player.inventory.sumOf { item ->
                if(item?.type != itemToObtain) 0
                else item.amount
            }
            val stillNeedToObtain = obtainAmount - alreadyObtained - alreadyOwns
            if(event.item.itemStack.amount >= stillNeedToObtain)
                this.setCompleted(player)
        }
    }

    private fun inventoryClick(event: InventoryClickEvent){
        val player = event.whoClicked as? Player ?: return
        if(!isPlayerAllowed(player)) return
        val cardItem = event.currentItem ?: return
        if(!TaskTussleSystem.clickItem.isThisItem(cardItem)) return
        val cursorItem = event.cursor

        // When amount = 1
        // we check this first, even though it would also work in the other one, this is of-course the faster approach
        if(obtainAmount == 1 &&
            cursorItem?.type == itemToObtain){
            this.setCompleted(player)
            if(handIn) cursorItem.amount -= 1
            return
        }

        // When amount is bigger than 1, and you have to hand it in, it will get removed from your hand
        if(handIn){
            onHandInMultiple(player,cursorItem)
            return
        }
        // When amount is bigger than 1, and you get to keep everything
        onKeepMultiple(player,cursorItem, false)
    }

    private fun onHandInMultiple(player :Player, cursorItem : ItemStack?){
        if(cursorItem?.type == itemToObtain){
            val obtainingNow = min(cursorItem.amount, obtainAmount - alreadyObtained)
            cursorItem.amount -= obtainingNow
            alreadyObtained += obtainingNow
            icon.refreshProgression()
            addContributor(player.name)
            if(alreadyObtained == obtainAmount)
                this.setCompleted(player)
            else
                player.playSound(player.location, Sound.ENTITY_LLAMA_CHEST, SoundCategory.MASTER, 0.2f, 1f)
        }
    }
    private fun onKeepMultiple(player: Player, cursorItem: ItemStack?, clickedOnIcon: Boolean){
        var currentlyObtained = 0
        if(cursorItem?.type == itemToObtain)
            currentlyObtained = cursorItem.amount

        // either you click with this item in your cursor, or you click on the task icon itself
        if(currentlyObtained > 0 || clickedOnIcon){
            if(currentlyObtained >= obtainAmount - alreadyObtained){
                // if this stack turns out to be enough already ,we don't have to loop through the whole inventory
                this.setCompleted(player)
                return
            }
            val obtainItems = player.inventory.filter { itemStack -> itemStack?.type == itemToObtain }
            currentlyObtained += obtainItems.sumOf { itemStack -> itemStack.amount }
            if(currentlyObtained >= obtainAmount - alreadyObtained)
                this.setCompleted(player)
        }
    }


    override fun enable() {
        handIn = ObtainTaskManager.handInItem
        if(!handIn)
            TaskEventsListener.entityPickupItemObservers.add(this::pickupEvent)
        // only when we are allowed to keep the item do we allow for pickups,
        // because we cant remove something from a stack in the pickup event for some reason
        TaskTussleSystem.log("enabling obtain task ${itemToObtain.name}")
        TaskEventsListener.inventoryClickObservers.add(this::inventoryClick)
    }
    override fun disable() {
        TaskEventsListener.entityPickupItemObservers.remove(this::pickupEvent)
        TaskEventsListener.inventoryClickObservers.remove(this::inventoryClick)
    }

    override fun clone(otherTeam : Team): ObtainTask {
        return ObtainTask(otherTeam, associatedSet, itemToObtain, obtainAmount)
    }
}
