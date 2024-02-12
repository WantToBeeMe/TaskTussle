package me.wanttobee.tasktussle.tasks.achievementTask

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskEventsListener
import me.wanttobee.tasktussle.base.tasks.TaskIcon
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.advancement.Advancement
import org.bukkit.event.player.PlayerAdvancementDoneEvent

class AdvancementsTask(associatedTeam : Team?, associatedSet: TeamSet<*>, val advancementToComplete: Advancement) : ITask(associatedTeam, associatedSet) {
    private val advancementTitle = advancementToComplete.display?.title ?: "Unknown Advancement"
    override val icon: TaskIcon = TaskIcon(Material.KNOWLEDGE_BOOK, advancementTitle,
        AdvancementsTaskManager.taskName, {"0/1"}, "complete this advancement")

    override fun getSuccessMessage(completerTeam: Team): String {
        return  "${completerTeam.getDisplayName()}${ChatColor.RESET} got an advancements task ${ChatColor.GRAY}($advancementTitle)"
    }

    private val advancementsEvent : (PlayerAdvancementDoneEvent) -> Unit = event@{ event ->
        val player = event.player
        if(!isPlayerAllowed(player)) return@event
        val advancement = event.advancement
        if (advancement.key == advancementToComplete.key)
            this.setCompleted(player)
    }

    override fun enable() {
        TaskTussleSystem.log("enabling advancement task: $advancementTitle")
        TaskEventsListener.advancementObservers.add(advancementsEvent)
    }
    override fun disable() {
        TaskEventsListener.advancementObservers.remove(advancementsEvent)
    }

    override fun clone(otherTeam: Team): ITask {
        return AdvancementsTask(otherTeam, associatedSet, advancementToComplete)
    }
}
