package me.wanttobee.tasktussle.tasks.obtainTask

import me.wanttobee.commandTree.commandTree.CommandBoolLeaf
import me.wanttobee.commandTree.commandTree.CommandStringLeaf
import me.wanttobee.commandTree.commandTree.ICommandNode
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.ITaskManager
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material

object ObtainTaskManager  : ITaskManager<ObtainTask>() {
    private var fileName = "default.yml"

    // handInItem boolean determent if the item you obtain will remove 1 from the stack or just leave it as is
    var handInItem = false
        private set

    override val taskTypeName: String = "obtain_task"
    override val settings: Array<ICommandNode> = arrayOf(
        CommandBoolLeaf("hand_in_item",
            { p,arg -> handInItem = arg; settingIsChangedTo(p,"hand in item", arg) },
            { p -> settingIsCurrently(p, "hand in item", handInItem) }),
        CommandStringLeaf("file_name", { ObtainTaskFiles.getAllFileNames() },
            { p,arg -> fileName = arg; settingIsChangedTo(p,"file_name", arg)},
            { p -> settingIsCurrently(p, "file_name",fileName) }),
    )

    override fun generateTasks(associatedTeam: Team, amounts: Triple<Int, Int, Int>, skip: List<ITask>): Array<ObtainTask>? {
        val taskPool = ObtainTaskFiles.readFile(fileName) ?: return null
        val realSkip : List<ObtainTask> = skip.filterIsInstance<ObtainTask>()
        val easyPool  =  taskPool.first .filter{mat -> !realSkip.any { task -> task.itemToObtain == mat }}.shuffled()
        val normalPool = taskPool.second.filter{mat -> !realSkip.any { task -> task.itemToObtain == mat }}.shuffled()
        val hardPool  =  taskPool.third .filter{mat -> !realSkip.any { task -> task.itemToObtain == mat }}.shuffled()

        val realEasyAmount = if(amounts.first > easyPool.size) easyPool.size else amounts.first
        val realHardAmount = if(amounts.third > hardPool.size) hardPool.size else amounts.third

        var normalAmount = amounts.second
        normalAmount += amounts.first - realEasyAmount
        normalAmount += amounts.third - realHardAmount
        val realNormalAmount = if(normalAmount > normalPool.size) normalPool.size else normalAmount

        val selectedMaterials = mutableListOf<Material>()
        selectedMaterials.addAll(easyPool.take(realEasyAmount))
        selectedMaterials.addAll(normalPool.take(realNormalAmount))
        selectedMaterials.addAll(hardPool.take(realHardAmount))
        selectedMaterials.shuffle()
        return Array(selectedMaterials.size) {i -> ObtainTask(selectedMaterials[i],associatedTeam ) }
    }

    override fun getExplanationText(clickItemName : String): String {
        return if(handInItem)
            "To submit an item, go in your inventory, drag the item you want to submit, and click with this item on the $clickItemName."
        else "To submit an item to the card, you will need to pick it up, or drag it on to your $clickItemName."
    }
}
