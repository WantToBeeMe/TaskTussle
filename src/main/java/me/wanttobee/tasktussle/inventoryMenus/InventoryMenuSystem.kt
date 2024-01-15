package me.wanttobee.tasktussle.inventoryMenus


import me.wanttobee.tasktussle.ItemUtil
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryDragEvent

object InventoryMenuSystem : Listener {
    private val inventories : MutableList<IInventoryMenu> = mutableListOf()

    fun addInventory(inv : IInventoryMenu) : Boolean{
        val contains = inventories.contains(inv)
        if(!contains)
            inventories.add(inv)
        return !contains
    }
    fun removeInventory(inv : IInventoryMenu) : Boolean{
        return inventories.remove(inv)
    }

    @EventHandler
    fun onInInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory
        val player = event.whoClicked as? Player ?: return

        for(inv in inventories){
            if(inv.isThisInventory(clickedInventory)){
                inv.clickEvent(player, event)
                return
            }
            else if(inv.isThisInventory(player.openInventory.topInventory)){
                inv.bottomClickEvent(player, event)
                return
            }
        }

    }
    @EventHandler
    fun onInInventoryDrag(event: InventoryDragEvent) {
        val clickedInventory = event.inventory
        val player = event.whoClicked as? Player ?: return

        for(inv in inventories){
            if(inv.isThisInventory(clickedInventory)){
                inv.dragEvent(player, event)
                return
            }
            else if(inv.isThisInventory(player.openInventory.topInventory)){
                inv.bottomDragEvent(player, event)
                return
            }
        }
    }
    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val clickedInventory = event.inventory
        val player = event.player as? Player ?: return

        for (inv in inventories) {
            if (inv.isThisInventory(clickedInventory)) {
                inv.closeEvent(player, event)
                return
            }
        }
    }

    fun debugStatus(commander : Player) {
        commander.sendMessage("${ItemUtil.title}${ChatColor.YELLOW}active menu's:")
        if(inventories.isEmpty())
            commander.sendMessage("${ChatColor.GOLD}none open!!")
        for(inv in inventories)
            commander.sendMessage("${ChatColor.GOLD}- ${ChatColor.WHITE}${inv::class.simpleName} ${ChatColor.GRAY}(${inv.amountViewers()} open)")
    }
}