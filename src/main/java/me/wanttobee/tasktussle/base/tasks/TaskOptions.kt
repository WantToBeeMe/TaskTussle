package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.util.toLore
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

// note that this inventory will always close whenever you use one of the tokens
// this is because you then set the state of the task to something that will disable the task
// while disabling the task, this inventory also always gets removed, thus closing it
class TaskOptions(private val task: ITask) : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null, InventoryType.HOPPER, task.icon.taskTitle)

    private val emptyToken = UniqueItemStack(Material.LIGHT_GRAY_CANDLE, "${ChatColor.GRAY}Empty token slot","${ChatColor.DARK_GRAY}a spot for a future token")
    private var currentIndex = 2

    init{
        TaskTussleSystem.log("adding task setting menu")
        // don't use the icon of the task itself,
        // that will break it, and you will be able to use that
        // item to instantly complete the obtain task
        val taskSettingIcon = UniqueItemStack(
            task.icon.iconMaterial,
            "${ChatColor.WHITE}${task.icon.taskTitle}",
            "${ChatColor.DARK_GRAY}Click: ${ChatColor.GRAY}go back to the card")
        addLockedItem(0, taskSettingIcon){ p,_ ->
            TaskTussleSystem.openCard(p)
        }
        addSeparator(1)
        for(i in 2 until inventory.size)
            addLockedItem(i,emptyToken)

        setSucceedToken()
        setSkipToken()
    }

    private fun setSkipToken(){
        val card = task.ownerCard ?: return
        if(card.skipTokensMax <= 0) return

        val skipToken = UniqueItemStack(
            if(card.skipTokens == 0) Material.GRAY_CANDLE else Material.RED_CANDLE,
            (if(card.skipTokens == 0) "${ChatColor.GRAY}" else "${ChatColor.RED}") + "Skip token",
            if(card.skipTokens == 0) listOf("${ChatColor.DARK_GRAY}0 left")
            else listOf("${ChatColor.GRAY}${card.skipTokens} left") +
                    "${ChatColor.DARK_GRAY}Skip tokens can be used to skip the task by failing it".toLore(35)
                    )
        addLockedItem(currentIndex++, skipToken){ _ , _ ->
            // one last check, it may be that someone is in another inventory, and uses this token
            // in that case this inventory is not accurate anymore, but we don't want the functionality to fail because of that
            // so we at least check it so the game does not break and people can't abuse it
            if(card.skipTokens > 0) {
                card.skipTokens--
                task.setFailed()
            }
        }
    }

    private fun setSucceedToken(){
        val card = task.ownerCard ?: return
        if(card.successTokensMax <= 0) return

        val succeedToken = UniqueItemStack(
            if(card.successTokens == 0) Material.GRAY_CANDLE else Material.LIME_CANDLE,
            (if(card.successTokens == 0) "${ChatColor.GRAY}" else "${ChatColor.GREEN}") + "Succeed token",
            if(card.successTokens == 0) listOf("${ChatColor.DARK_GRAY}0 left")
            else listOf("${ChatColor.GRAY}${card.successTokens} left") +
                    "${ChatColor.DARK_GRAY}Succeed tokens can be used to complete the task instantly".toLore(35)
                    )
        addLockedItem(currentIndex++, succeedToken){ player , _ ->
            // one last check, it may be that someone is in another inventory, and uses this token
            // in that case this inventory is not accurate anymore, but we don't want the functionality to fail because of that
            // so we at least check it so the game does not break and people can't abuse it
            if(card.successTokens > 0) {
                card.successTokens--
                task.setCompleted(player)
            }
        }
    }

    // we only want this inventory to exist whenever we open it
    override fun closeEvent(player: Player, event: InventoryCloseEvent) {
        // THIS IS THE CLEAR EVENT, WITHOUT THE BIT THAT CLOSES ALL THE VIEWER
        separator.unsubscribe(this)
        emptyToken.unsubscribe(this)

        InteractiveInventorySystem.removeInventory(this)
        TaskTussleSystem.log("removed task setting menu")
    }
}
