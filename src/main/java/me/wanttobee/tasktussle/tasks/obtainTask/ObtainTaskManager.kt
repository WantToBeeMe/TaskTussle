package me.wanttobee.tasktussle.tasks.obtainTask

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.tasktussle.Util.toLore
import me.wanttobee.tasktussle.generic.TaskTussleSettings
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.ITaskManager
import me.wanttobee.tasktussle.tasks.achievementTask.AdvancementsFiles
import me.wanttobee.tasktussle.tasks.achievementTask.AdvancementsTaskManager
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material

object ObtainTaskManager : ITaskManager<ObtainTask>(Material.SHULKER_SHELL, "Obtain Task",
    "Obtain 1 or more of the randomly selected item (displayed on the task)") {

    // handInItem boolean determent if the item you obtain will remove 1 from the stack or just leave it as is
    var handInItem = false
        private set

    // the amount the task has to obtain (for example, obtain 64 dirt)
    private var easyCount = 1
    private var normalCount = 1
    private var hardCount = 1

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
        addFileSetting(ObtainTaskFiles)

        val countLore = listOf(
            "${ChatColor.DARK_GRAY}Shift+L Click:${ChatColor.GRAY} +10",
            "${ChatColor.DARK_GRAY}L Click:${ChatColor.GRAY} +1",
            "${ChatColor.DARK_GRAY}R Click:${ChatColor.GRAY} -1",
            "${ChatColor.DARK_GRAY}Shift+R Click:${ChatColor.GRAY} -10",
        )
        // easy count
        val easyCountIcon = UniqueItemStack(Material.BRICK,"", countLore)
            .updateEnchanted(handInItem)
        settingsInventory.addSetting(easyCountIcon,{
            easyCountIcon.updateTitle(
                "${TaskTussleSettings.settingColor}Easy obtain count:${ChatColor.YELLOW} $easyCount"
            ).pushUpdates()
        }, {_,shift -> easyCount += if(shift) 10 else 1},
            {_,shift ->
                easyCount -= if(shift) 10 else 1
                if(easyCount < 1) easyCount = 1
            })

        // normal count
        val normalCountIcon = UniqueItemStack(Material.NETHER_BRICK,"", countLore)
            .updateEnchanted(handInItem)
        settingsInventory.addSetting(normalCountIcon,{
            normalCountIcon.updateTitle(
                "${TaskTussleSettings.settingColor}Normal obtain count:${ChatColor.YELLOW} $normalCount"
            ).pushUpdates()
        }, {_,shift -> normalCount += if(shift) 10 else 1},
            {_,shift ->
                normalCount -= if(shift) 10 else 1
                if(normalCount < 1) normalCount = 1
            })

        // hard count
        val hardCountIcon = UniqueItemStack(Material.NETHERITE_INGOT,"", countLore)
            .updateEnchanted(handInItem)
        settingsInventory.addSetting(hardCountIcon,{
            hardCountIcon.updateTitle(
                "${TaskTussleSettings.settingColor}Hard obtain count:${ChatColor.YELLOW} $hardCount"
            ).pushUpdates()
        }, {_,shift -> hardCount += if(shift) 10 else 1},
            {_,shift ->
                hardCount -= if(shift) 10 else 1
                if(hardCount < 1) hardCount = 1
            })
    }

    override fun generateTasks(associatedTeam: Team, amounts: Triple<Int, Int, Int>, skip: List<ITask>): Array<ObtainTask>? {
        val taskPool = ObtainTaskFiles.readFile(fileName!!) ?: return null
        val realSkip : List<ObtainTask> = skip.filterIsInstance<ObtainTask>()
        val easyPool  =  taskPool.first .filter{mat -> !realSkip.any { task -> task.itemToObtain == mat }}.shuffled()
        val normalPool = taskPool.second.filter{mat -> !realSkip.any { task -> task.itemToObtain == mat }}.shuffled()
        val hardPool  =  taskPool.third .filter{mat -> !realSkip.any { task -> task.itemToObtain == mat }}.shuffled()

        val realAmounts = shiftAmounts(amounts, easyPool.size, normalPool.size, hardPool.size)

        val selectedMaterials = mutableListOf<ObtainTask>()
        selectedMaterials.addAll(easyPool.take(realAmounts.first).map {
            material -> ObtainTask(associatedTeam, material, easyCount ) })
        selectedMaterials.addAll(normalPool.take(realAmounts.second).map {
            material -> ObtainTask(associatedTeam, material, normalCount ) })
        selectedMaterials.addAll(hardPool.take(realAmounts.third).map {
            material -> ObtainTask(associatedTeam, material, hardCount ) })
        val arrayTasks = selectedMaterials.toTypedArray()
        arrayTasks.shuffle()
        return arrayTasks
    }

    override fun getExplanationText(clickItemName : String): String {
        return if(handInItem)
            "To complete an obtain task, go in your inventory, drag the item you want to submit, and click with this item on the $clickItemName."
        else "To complete an obtain task, you will need to pick its corresponding item up, or drag it on to your $clickItemName."
    }
}
