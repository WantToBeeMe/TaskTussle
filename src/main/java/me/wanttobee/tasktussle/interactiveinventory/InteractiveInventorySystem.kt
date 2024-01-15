package me.wanttobee.tasktussle.interactiveinventory

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent

object InteractiveInventorySystem : Listener {
    private val inventories : MutableList<InteractiveInventory> = mutableListOf()

    // if an item has been added, that means that this item will be taken in account whenever an event happens,
    // this means that whenever someone clicks an item, every interactiveItemInventory that has been added will be checked if it was that one
    // that's why it is important to remove the item from the list when you are done with it
    // (but don't worry, interactiveItemInventory.clear() does that all on its own)
    fun addInventory(inv : InteractiveInventory){
        if(!inventories.contains(inv)){
            inventories.add(inv)
        }
    }
    fun removeInventory(inv : InteractiveInventory) :Boolean{
        return inventories.remove(inv)
    }

    @EventHandler
    fun iClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory
        val player = event.whoClicked as? Player ?: return
        for(inv in inventories){
            if(inv.inventory == clickedInventory){
                inv.onClick(player, event)
                event.isCancelled = true;
                return
            }
        }
    }
    @EventHandler
    fun iDrag(event: InventoryDragEvent) {
        val clickedInventory = event.inventory
        val player = event.whoClicked as? Player ?: return
        for(inv in inventories){
            if(inv.inventory == clickedInventory){
                inv.onDrag(player, event)
                event.isCancelled = true;
                return
            }
        }
    }

    // this method will print out all the active interactive inventories to the player provided
    // is handy for debugging purposes (to see if you closed an inventory correctly)
    fun debugStatus(commander: Player){
        commander.sendMessage("${ChatColor.AQUA}Interactive Inventory:")
        for(i in inventories)
            i.debugStatus(commander)
    }

    fun disablePlugin(){
        val iClone =inventories.toTypedArray()
        for(inventory in iClone){
            inventories.clear()
        }
    }
}