package me.wanttobee.tasktussle.base.generic

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryType
import kotlin.math.max

object TimeSettings : InteractiveInventory()  {
    override var inventory = Bukkit.createInventory(null, InventoryType.DROPPER, "Time Settings")

    private val timeIcon = UniqueItemStack(Material.CLOCK,
        "${TaskTussleSettings.settingColor}Game Time: ${ChatColor.RED}disabled",
        "${ChatColor.GRAY}Click to go back")

    init{
        fillGapsWithSeparator()

        addLockedItem(4, timeIcon) {p,_ -> TaskTussleSettings.open(p)}

        val disableTime = UniqueItemStack(Material.BARRIER, "${ChatColor.RED}disable",null)
        val doMinOne = UniqueItemStack(Material.RED_CANDLE, "${ChatColor.RED}-1",null)
        val doMinTen = UniqueItemStack(Material.RED_WOOL, "${ChatColor.RED}-10",null)
        val doPlusOne = UniqueItemStack(Material.LIME_CANDLE, "${ChatColor.GREEN}+1",null)
        val doPlusTen = UniqueItemStack(Material.LIME_WOOL, "${ChatColor.GREEN}+10",null)

        addLockedItem(0,doMinOne) {_,_ ->
            TaskTussleSystem.gameTime = max(0,TaskTussleSystem.gameTime-1)
            updateTimeIcon()
        }
        addLockedItem(6,doMinTen) {_,_ ->
            TaskTussleSystem.gameTime = max(0,TaskTussleSystem.gameTime-10)
            updateTimeIcon()
        }
        addLockedItem(2,doPlusOne) {_,_ ->
            TaskTussleSystem.gameTime += 1
            updateTimeIcon()
        }
        addLockedItem(8,doPlusTen) {_,_ ->
            TaskTussleSystem.gameTime += 10
            updateTimeIcon()
        }
        addLockedItem(7,disableTime) {_,_ ->
            TaskTussleSystem.gameTime = 0
            updateTimeIcon()
        }
    }

    private fun updateTimeIcon(){
        val newTitle = "${TaskTussleSettings.settingColor}Game Time: " +
                if (TaskTussleSystem.gameTime != 0) "${ChatColor.GREEN}${TaskTussleSystem.gameTime} minutes"
                else "${ChatColor.RED}disabled"
        timeIcon
            .updateEnchanted(TaskTussleSystem.gameTime > 0)
            .updateTitle(newTitle)
            .pushUpdates()
    }
}
