package me.wanttobee.tasktussle.tasks.obtainTask

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.tasktussle.Util.toLore
import me.wanttobee.tasktussle.generic.TaskTussleSettings
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.ITaskManager
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material

object ObtainTaskManager : ITaskManager<ObtainTask>(Material.SHULKER_SHELL, "Obtain Task",
    "Obtain 1 or more of the randomly selected item (displayed on the task)") {
    private var fileName = "default.yml"

    // handInItem boolean determent if the item you obtain will remove 1 from the stack or just leave it as is
    var handInItem = false
        private set

    init{
        // hand in items
        val handInIcon = UniqueItemStack(Material.HOPPER,"", null)
        settingsInventory.addSetting(handInIcon,{
            val newTitle ="${TaskTussleSettings.settingColor}Hand in items: " +
                    if(handInItem) "${ChatColor.GREEN}on"
                    else "${ChatColor.RED}off"
            handInIcon.updateTitle(newTitle)
                .updateEnchanted(handInItem)
                .pushUpdates()
        }){_,_ -> handInItem = !handInItem}

        // fileName
        var fileNameIndex = 0
        var fileNameIconSwap = false
        val fileNameIcon = UniqueItemStack(Material.PAPER,"",
            "${ChatColor.GRAY}Click to loop through the different options".toLore(32))
            .updateEnchanted(handInItem)
        settingsInventory.addSetting(fileNameIcon,{
            fileNameIcon.updateTitle(
                "${TaskTussleSettings.settingColor}File name:${ChatColor.YELLOW} $fileName"
            ).updateMaterial(if(fileNameIconSwap) Material.PAPER else Material.MAP)
                .pushUpdates()
        }){_,_ ->
            fileNameIconSwap = !fileNameIconSwap
            val options = ObtainTaskFiles.getAllFileNames()
            fileNameIndex = (fileNameIndex+1)%options.size
            fileName = options[fileNameIndex]
        }
    }

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
            "To complete an obtain task, go in your inventory, drag the item you want to submit, and click with this item on the $clickItemName."
        else "To complete an obtain task, you will need to pick its corresponding item up, or drag it on to your $clickItemName."
    }
}
