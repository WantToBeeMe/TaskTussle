package me.wanttobee.tasktussle.interactiveinventory

import me.wanttobee.tasktussle.ItemUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.inventory.ItemStack

class InteractiveInventory(size : Int,private val title : String) {
    protected val separator = ItemUtil.itemFactory(Material.BLACK_STAINED_GLASS_PANE, " ", null)
    val inventory = Bukkit.createInventory(null, size, title)
    // the itemStack represents the item that has to be clicked in order to activate the events
    // the pair represents the events that will happen, first being left-clicked, second in the pair being right-clicked
    private val clickEvents : MutableMap<ItemStack, Pair<((Player) -> Unit)?,((Player) -> Unit)?>> = mutableMapOf()

    init{
        InteractiveInventorySystem.addInventory(this)
    }

    // will make sure everyone who has this inventory open currently, will be dropped out of the inventory
    fun closeViewers(){
        for (viewerID in inventory.viewers.indices) {
            if (inventory.viewers.size > viewerID
                && inventory.viewers[viewerID] is Player
                && inventory.viewers[viewerID].openInventory.topInventory == inventory) {
                inventory.viewers[viewerID].closeInventory()
            }
        }
    }
    fun clear(){
        closeViewers()
        InteractiveInventorySystem.removeInventory(this)
    }

    // will open this inventory to that player provided
    fun open(player : Player){
        player.openInventory(inventory)
    }

    fun setItem(slot: Int, item: ItemStack){
        inventory.setItem(slot, item)
    }

    fun itemClickEvent(item: ItemStack, onClick  : ((Player) -> Unit)? = null){
        return itemClickEvent(item, onClick,onClick)
    }
    fun itemClickEvent(item : ItemStack, onLeftClick : ((Player) -> Unit)?, onRightClick : ((Player) -> Unit)?){
        if(onLeftClick == null && onRightClick == null){
            clickEvents.remove(item)
            return
        }
        clickEvents[item] = Pair(onLeftClick, onRightClick)
    }


    fun moveClickEvent(from : ItemStack, to: ItemStack){
        if(clickEvents.containsKey(from))
            clickEvents[to] = clickEvents[from]!!
        clickEvents.remove(from)
    }

    fun onClick(player: Player, event : InventoryClickEvent){
        val item = event.currentItem ?: return
        val itemEvent  = clickEvents[item] ?: return
        val clickType = event.click
        if(clickType.isLeftClick)
            itemEvent.first?.invoke(player)
        if(clickType.isRightClick)
            itemEvent.second?.invoke(player)
    }

    fun onDrag(player: Player, event : InventoryDragEvent){}

    fun debugStatus(commander : Player){
        commander.sendMessage("${ChatColor.AQUA}IInv - $title ${ChatColor.AQUA}clickEvents:")
        for((stack, _) in clickEvents)
            commander.sendMessage("${ChatColor.WHITE} - ${ChatColor.AQUA}${stack.itemMeta?.displayName}")
    }
}
