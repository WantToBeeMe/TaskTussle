package me.wanttobee.tasktussle.base.games

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.base.tasks.ITask
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.Inventory

abstract class ITTCardGui(inventorySlotAmount : Int, inventoryTitle: String) : InteractiveInventory() {
    companion object{
        val emptyTeamIcon = UniqueItemStack(Material.GRAY_STAINED_GLASS, "${ChatColor.RESET}${ChatColor.GRAY}Empty Card", null)
    }
    override var inventory: Inventory = Bukkit.createInventory(null, inventorySlotAmount, inventoryTitle)

    abstract fun displayTask(taskSet: Array<ITask>)
}
