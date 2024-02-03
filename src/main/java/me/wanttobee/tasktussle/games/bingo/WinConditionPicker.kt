package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class WinConditionPicker : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null, 9, "Win condition picker")

    init{
        TaskTussleSystem.log("created win-condition selector menu")
        val winConditions = arrayOf(
            Pair(Material.SMALL_AMETHYST_BUD,   "1 line"),
            Pair(Material.MEDIUM_AMETHYST_BUD,  "2 lines"),
            Pair(Material.LARGE_AMETHYST_BUD,   "3 lines"),
            Pair(Material.AMETHYST_CLUSTER,     "4 lines"),
            Pair(Material.AMETHYST_BLOCK,       "full card"),
            Pair(Material.AIR, ""),
            Pair(Material.AMETHYST_SHARD,       "horizontal line"),
            Pair(Material.AMETHYST_SHARD,       "vertical line"),
            Pair(Material.AMETHYST_SHARD,       "diagonal line")
        )

        for(pairIndex in winConditions.indices){
            val pair = winConditions[pairIndex]
            if(pair.first == Material.AIR) {
                addSeparator(pairIndex)
                continue
            }
            val icon = UniqueItemStack(pair.first, "${ChatColor.LIGHT_PURPLE}${pair.second}",null)
            if(BingoGameManager.winningCondition == pair.second)
                icon.updateEnchanted(true)
                    .updateLore(listOf("${ChatColor.GRAY}current"))

            addLockedItem(pairIndex,icon){p,_ ->
                BingoGameManager.winningCondition = pair.second
                BingoGameManager.settingsInventory.open(p)
            }
        }
    }

    // we only want this inventory to exist whenever we open it
    override fun closeEvent(player: Player, event: InventoryCloseEvent) {
        // THIS IS THE CLEAR EVENT, WITHOUT THE BIT THAT CLOSES ALL THE VIEWER
        separator.unsubscribe(this)
        InteractiveInventorySystem.removeInventory(this)
        TaskTussleSystem.log("removed win-condition selector menu")
    }
}
