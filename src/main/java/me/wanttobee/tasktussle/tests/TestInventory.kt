package me.wanttobee.tasktussle.tests

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack

import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class TestInventory(title : String) : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null, InventoryType.DROPPER, title)

    init{
        this.fillGapsWithSeparator()

        val goldBlock = UniqueItemStack(Material.GOLD_BLOCK, "GOLD!!",null)
        var isOn = true
        val goldSlot = 8
        this.addLockedItem(goldSlot,goldBlock) {clicker ->
            if(isOn)
                goldBlock.updateMaterial(Material.WHITE_WOOL.colorize(ChatColor.YELLOW))
            else
                goldBlock.updateMaterial(Material.GOLD_BLOCK)
            goldBlock.updateTitle(clicker.name).pushUpdates()
            isOn = !isOn
        }
    }
}
