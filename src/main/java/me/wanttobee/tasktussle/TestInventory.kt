package me.wanttobee.tasktussle

import me.wanttobee.everythingitems.ItemUtil
import me.wanttobee.everythingitems.ItemUtil.colorize

import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

class TestInventory(title : String) : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null, InventoryType.DROPPER, title)

    init{

        this.fillGapsWithSeparator()

        val block = ItemUtil.itemFactory(Material.GOLD_BLOCK, "GOLD!",null)
        val wool = ItemUtil.itemFactory(Material.WHITE_WOOL.colorize(ChatColor.YELLOW), "GOLD!",null)
        var isOn = true
        this.addLockedItem(0,block) {_ ->
            if(isOn) this.editItem(block,wool)
            else this.editItem(wool,block)
            isOn = !isOn
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