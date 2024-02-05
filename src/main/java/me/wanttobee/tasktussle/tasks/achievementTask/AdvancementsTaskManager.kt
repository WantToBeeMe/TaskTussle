package me.wanttobee.tasktussle.tasks.achievementTask

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.Util.toLore
import me.wanttobee.tasktussle.generic.TaskTussleSettings
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.ITaskManager
import me.wanttobee.tasktussle.tasks.obtainTask.ObtainTaskManager
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.advancement.AdvancementProgress


object AdvancementsTaskManager : ITaskManager<AdvancementsTask>(Material.KNOWLEDGE_BOOK, "Advancements Task",
    "Get an advancement (displayed on the task)") {

    init{
        setOccupationRatio(2)
        // fileName
        addFileSetting(AdvancementsFiles)
    }

    override fun generateTasks(associatedTeam: Team, amounts: Triple<Int, Int, Int>, skip: List<ITask>): Array<AdvancementsTask>? {
        val taskPool = AdvancementsFiles.readFile(fileName!!) ?: return null
        val realSkip : List<AdvancementsTask> = skip.filterIsInstance<AdvancementsTask>()
        val easyPool  =  taskPool.first .filter{adv -> !realSkip.any { task -> task.advancementToComplete.key == adv }}.shuffled()
        val normalPool = taskPool.second.filter{adv -> !realSkip.any { task -> task.advancementToComplete.key == adv }}.shuffled()
        val hardPool  =  taskPool.third .filter{adv -> !realSkip.any { task -> task.advancementToComplete.key == adv }}.shuffled()

        val realAmounts = shiftAmounts(amounts,easyPool.size,normalPool.size, hardPool.size )

        val selectedAdvancements = mutableListOf<AdvancementsTask>()
        selectedAdvancements.addAll(
            easyPool.take(realAmounts.first).mapNotNull { namespace ->
                Bukkit.getAdvancement(namespace)?.let { AdvancementsTask(associatedTeam, it) }
            })
        selectedAdvancements.addAll(
            normalPool.take(realAmounts.second).mapNotNull { namespace ->
                Bukkit.getAdvancement(namespace)?.let { AdvancementsTask(associatedTeam, it) }
            })
        selectedAdvancements.addAll(
            hardPool.take(realAmounts.third).mapNotNull { namespace ->
                Bukkit.getAdvancement(namespace)?.let { AdvancementsTask(associatedTeam, it) }
            })
        val arrayTasks = selectedAdvancements.toTypedArray()
        arrayTasks.shuffle()
        return arrayTasks
    }

    override fun prepareForThisTaskType(teamSet: TeamSet<*>) {
        if (TaskTussleSystem.hideCard) {
            TaskTussleSystem.minecraftPlugin.server.worlds.forEach { world ->
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            }
        }
        for(advancement in Bukkit.advancementIterator()){
            teamSet.forEachPlayer {player ->
                val progress: AdvancementProgress = player.getAdvancementProgress(advancement)
                for (criteria in progress.awardedCriteria)
                    progress.revokeCriteria(criteria!!)
            }
        }
    }

    override fun getExplanationText(clickItemName: String): String? { return null }
}
