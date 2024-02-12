package me.wanttobee.tasktussle.tasks.clickTask

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.util.toLore
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskIcon
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class ClickTask(associatedTeam : Team?, associatedSet: TeamSet<*>): ITask(associatedTeam, associatedSet) {
    override val icon: TaskIcon = TaskIcon(Material.BELL, "CLICK ME", ClickTaskManager.taskName, {"0/1"}, "click me")

    override fun getSuccessMessage(completerTeam: Team): String {
        return "${completerTeam.getDisplayName()}${ChatColor.RESET} clicked"
    }

    override fun onLeftClickIcon(player: Player, shift: Boolean, event: InventoryClickEvent) {
        this.setCompleted(player)
    }

    override fun enable() {
        TaskTussleSystem.log("enabling a click task")
    }
    override fun disable() {}

    override fun clone(otherTeam: Team): ITask {
        return ClickTask(otherTeam, associatedSet)
    }
}
