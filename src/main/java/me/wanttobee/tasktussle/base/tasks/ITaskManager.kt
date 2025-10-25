package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.tasktussle.util.toLore
import me.wanttobee.tasktussle.base.generic.IManager
import me.wanttobee.tasktussle.base.generic.ManagerSettings
import me.wanttobee.tasktussle.base.generic.TaskTussleSettings
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.Material
import kotlin.math.max
import kotlin.math.min

abstract class ITaskManager<T : ITask>(taskIconMaterial : Material, taskName: String, taskDescription : String, settingsRows : Int = 1) :
    IManager(taskIconMaterial, taskName, taskDescription) {
    var occupationRatio = 10
        private set

    // just an alias
    val taskName : String
        get() = this.subjectName

    // By default, the file name is not set, for tasks that don't use any files, however,
    // You can make it so the filename is set with addFileSetting() and give it the appropriate TaskFile manager
    // initial will default to "default.yml" but you can also change this by entering another string in the second argument
    var fileName : String? = null
        set(value) {
            if (value == null)
                throw IllegalArgumentException("File name cannot be null")
            field = value
        }

    val settingsInventory =  ManagerSettings(this, settingsRows)

    fun setOccupationRatio(n : Int) : Boolean{
        occupationRatio = max(0,min(n, 100))
        return occupationRatio == n
    }
    fun changeOccupationRatioBy(n: Int) : Boolean{
        return setOccupationRatio(occupationRatio + n)
    }

    // this is to write your own implementation for generating tasks with the different difficulties
    abstract fun generateTasks(amounts : Triple<Int,Int,Int>, skip: Collection<ITask> = emptyList() ) : Array<T>?

    // clickItemName is the item that is used for the game, most of the time it's called TaskTussleCard,
    // but we don't want to be stuck to this name, so doing this we make sure that if we change it, it will be changed everywhere
    abstract fun getExplanationText(clickItemName : String) : String?

    // this method returns almost always the same amount back as you provided
    // however, if it turns out that the easy list is a little bit smaller, it will be caught here and correcter for in the other pools to come to the same amount again
    protected fun shiftAmounts(amounts: Triple<Int, Int, Int>, easyPoolSize: Int,normalPoolSize: Int, hardPoolSize: Int ) : Triple<Int, Int, Int>{
        val realEasyAmount = if(amounts.first > easyPoolSize) easyPoolSize else amounts.first
        val realHardAmount = if(amounts.third > hardPoolSize) hardPoolSize else amounts.third

        var normalAmount = amounts.second
        // here we add to the normalAmount what could not be completed by the hard and easy amounts
        normalAmount += amounts.first - realEasyAmount
        normalAmount += amounts.third - realHardAmount
        val realNormalAmount = if(normalAmount > normalPoolSize) normalPoolSize else normalAmount
        return Triple(realEasyAmount,realNormalAmount,realHardAmount)
    }

    open fun prepareForThisTaskType(teamSet: TeamSet<*>){}

    protected fun addFileSetting(fileManager: ITaskFiles, initialFileName: String = "default.yml"){
        fileName = initialFileName

        val fileNameIcon = UniqueItemStack(Material.NAME_TAG,"",
            "${ChatColor.GRAY}Click to change the file that will be used to generate the tasks".toLore(32))
            .updateEnchanted(true)
        settingsInventory.addSetting(fileNameIcon,{
            fileNameIcon.updateTitle(
                "${TaskTussleSettings.settingColor}File name:${ChatColor.YELLOW} $fileName"
            ).pushUpdates()
        }){p,_ -> FileNamePicker(fileManager,this).open(p) }
    }
}
