package me.wanttobee.tasktussle.tasks.clickTask

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.Util.toLore
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskEventsListener
import me.wanttobee.tasktussle.base.tasks.TaskIcon
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ClickTask(associatedTeam : Team?, associatedSet: TeamSet<*>): ITask(associatedTeam, associatedSet) {
    override val icon: TaskIcon = TaskIcon(Material.BELL, "CLICK ME", ClickTaskManager.taskName, {"0/1"}, "Click Me".toLore(32))

    override fun getSuccessMessage(completerTeam: Team): String {
        return "${completerTeam.getDisplayName()}${ChatColor.RESET} clicked"
    }

    private val inventoryClick : (InventoryClickEvent) -> Unit = event@{ event ->
        val player = event.whoClicked as? Player ?: return@event
        if(!isPlayerAllowed(player)) return@event
        val cardItem = event.currentItem ?: return@event
        if(icon.isThisItem(cardItem))
            this.setCompleted(player)
    }

    override fun enable() {
        TaskTussleSystem.log("enabling a click task")
        TaskEventsListener.inventoryClickObservers.add(inventoryClick)
    }
    override fun disable() {
        TaskEventsListener.inventoryClickObservers.remove(inventoryClick)
    }

    override fun clone(otherTeam: Team): ITask {
        return ClickTask(otherTeam, associatedSet)
    }
}