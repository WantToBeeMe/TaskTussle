package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.tasktussle.teams.Team
import org.bukkit.Material
import kotlin.math.max
import kotlin.math.min

abstract class ITaskManager<T : ITask>(val taskIconMaterial : Material, val taskName: String, val taskDescription : String) {
    var occupationRatio = 10
        private set

    val settingsInventory = TaskSettings(this)

    fun setOccupationRatio(n : Int) : Boolean{
        occupationRatio = max(0,min(n, 100))
        return occupationRatio == n
    }
    fun changeOccupationRatioBy(n: Int) : Boolean{
        return setOccupationRatio(occupationRatio + n)
    }

    // this is to write your own implementation for generating tasks with the different difficulties
    abstract fun generateTasks(associatedTeam : Team, amounts : Triple<Int,Int,Int>, skip: List<ITask> = emptyList() ) : Array<T>?

    // clickItemName is the item that is used for the game, most of the time it's called TaskTussleCard,
    // but we don't want to be stuck to this name, so doing this we make sure that if we change it, it will be changed everywhere
    abstract fun getExplanationText(clickItemName : String) : String?
}
