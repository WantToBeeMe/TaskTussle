package me.wanttobee.tasktussle.tasks.achievementTask

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.TaskEventsListener
import me.wanttobee.tasktussle.generic.tasks.TaskIcon
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.advancement.Advancement
import org.bukkit.event.player.PlayerAdvancementDoneEvent

class AdvancementsTask(associatedTeam : Team, val advancementToComplete: Advancement) : ITask(associatedTeam) {
    private val advancementTitle = advancementToComplete.display?.title ?: "Unknown Advancement"
    override val successMessage =
        "${associatedTeam.getDisplayName()}${ChatColor.RESET} got an advancements task ${ChatColor.GRAY}($advancementTitle)"
    override val icon: TaskIcon = TaskIcon(Material.KNOWLEDGE_BOOK, advancementTitle,
        AdvancementsTaskManager.taskName, {"0/1"}, listOf("get this advancement"))

    private val advancementsEvent : (PlayerAdvancementDoneEvent) -> Unit = event@{ event ->
        val player = event.player
        if(!associatedTeam.containsMember(player)) return@event
        val advancement = event.advancement
        if (advancement.key == advancementToComplete.key)
            this.setCompleted()
    }

    override fun enable() {
        TaskTussleSystem.log("enabling advancement task: $advancementTitle")
        TaskEventsListener.advancementObservers.add(advancementsEvent)
    }
    override fun disable() {
        TaskEventsListener.advancementObservers.remove(advancementsEvent)
    }

    override fun clone(otherTeam: Team): ITask {
        return AdvancementsTask(otherTeam, advancementToComplete)
    }
}
