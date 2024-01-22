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

        var goldBlock = UniqueItemStack(Material.GOLD_BLOCK, "GOLD!!",null)
        var isOn = true
        val goldSlot = 8
        this.addLockedItem(goldSlot,goldBlock) {clicker ->
            if(isOn) {
                val newItem = UniqueItemStack(Material.WHITE_WOOL.colorize(ChatColor.YELLOW), clicker.name,null)
                swapItem(goldBlock, newItem )
                goldBlock = newItem
            }
            else {
                val newItem = UniqueItemStack(Material.GOLD_BLOCK, clicker.name,null)
                swapItem(goldBlock, newItem )
                goldBlock = newItem
            }
            isOn = !isOn
        }


        val diamondBlock = UniqueItemStack(Material.DIAMOND_BLOCK, "DIAMOND!!",null)
        val diamondSlot = 0
        this.addLockedItem(diamondSlot,diamondBlock) {clicker ->
            val meta = diamondBlock.itemMeta
            meta!!.setDisplayName(clicker.name)
            diamondBlock.setItemMeta(meta)
            //updateItem(diamondBlock)
        }
        //for(i in alignments.indices){
        //    val item = if(currentAlignment != pair.second) SBUtil.itemFactory(Material.GRAY_STAINED_GLASS, "${ChatColor.YELLOW}Set Alignment to ${pair.first}", null)
        //    else SBUtil.itemFactory(Material.ORANGE_STAINED_GLASS, "${ChatColor.GOLD}Currently ${pair.first}", null)
        //    this.addLockedItem(i, item) {player ->
        //        if(currentAlignment != pair.second)
        //            StorySystem.getPlayersStory(player).alignment = pair.second
        //        closeEvent.invoke()
        //    }
        //}
    }


}