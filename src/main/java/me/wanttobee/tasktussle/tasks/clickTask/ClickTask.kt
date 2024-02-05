package me.wanttobee.tasktussle.tasks.clickTask

import me.wanttobee.everythingitems.ItemUtil.getRealName
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.Util.toLore
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.TaskEventsListener
import me.wanttobee.tasktussle.generic.tasks.TaskIcon
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ClickTask(associatedTeam : Team): ITask(associatedTeam) {
    override val icon: TaskIcon = TaskIcon(Material.BELL, "CLICK ME", ClickTaskManager.taskName, {"0/1"}, "Click Me".toLore(32))
    override val successMessage = "${associatedTeam.getDisplayName()}${ChatColor.RESET} clicked"

    private val inventoryClick : (InventoryClickEvent) -> Unit = event@{ event ->
        val player = event.whoClicked as? Player ?: return@event
        if(!associatedTeam.containsMember(player)) return@event
        val cardItem = event.currentItem ?: return@event
        if(icon.isThisItem(cardItem))
            this.setCompleted()
    }

    override fun enable() {
        TaskTussleSystem.log("enabling a click task")
        TaskEventsListener.inventoryClickObservers.add(inventoryClick)
    }
    override fun disable() {
        TaskEventsListener.inventoryClickObservers.remove(inventoryClick)
    }

    override fun clone(otherTeam: Team): ITask {
        return ClickTask(otherTeam)
    }
}