package me.wanttobee.tasktussle.tasks.clickTask

import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.ITaskManager
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.Material

object ClickTaskManager : ITaskManager<ClickTask>(Material.BELL,"Click Task",
    "Clicking on this task icon will complete the task. This task is mostly for testing purposes. " +
            "IDK why you would want to put this in your game, but if you really want to, you can. Its your game after all, so not my problem.") {
    override fun generateTasks(associatedTeam: Team?, associatedSet: TeamSet<*>, amounts: Triple<Int, Int, Int>, skip: List<ITask>): Array<ClickTask> {
       return Array(amounts.first + amounts.second + amounts.third) { ClickTask(associatedTeam, associatedSet) }
    }

    init{
        setOccupationRatio(0)
    }

    override fun getExplanationText(clickItemName: String): String? {
        return null
    }
}
