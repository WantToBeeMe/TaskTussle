package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.tasktussle.tasks.obtainTask.ObtainTaskManager
import me.wanttobee.tasktussle.teams.Team

object TaskFactory {
    private val taskManagers : List<ITaskManager<*>> = listOf(
        ObtainTaskManager,
    )

    fun <T: ITask, U: ITask> combineTasks(first: Array<T>, second : Array<U>, team: Team) : Array<ITask>{
        return (first.map { it.clone(team) } + second.map { it.clone(team) } ).toTypedArray()
    }
    private fun <T: ITask, U: ITask> unSaveCombineTasks(first: Array<T>, second : Array<U>) : Array<ITask>{
        return (first.toList() + second ).toTypedArray()
    }

    // this calculates the best ratio split
    // the amount parameter is the total amount you want to end up with (by adding easy + normal + hard)
    // then the easy normal and hard ratio ar the ratio that you are beginning with (and if those match the amount, you will also get that back)
    private fun calculateRatioAmount(amount: Int, easyRatio:Int, normalRatio:Int, hardRatio:Int) : Triple<Int,Int,Int> {
        val totalRatio = easyRatio + normalRatio + hardRatio
        val easyAmount = (easyRatio.toDouble() / totalRatio.toDouble() * amount).toInt()
        val hardAmount = (hardRatio.toDouble() / totalRatio.toDouble() * amount).toInt()
        val normalAmount = amount - easyAmount - hardAmount
        return Triple(easyAmount,normalAmount,hardAmount)
    }

    // this method returns null if it's unable to generate tasks (ratio is impossible or every task is disabled)
    fun createTasks(associatedTeam : Team, amount : Int, easyRatio : Int, normalRatio : Int, hardRatio: Int, skip: List<ITask> = emptyList() ) : Array<ITask>?{
        val totalRatio = easyRatio + normalRatio + hardRatio
        if(totalRatio == 0) return null
        // we put all the task in the pool that are enabled
        val enabledManagersList = taskManagers.filter{ manager -> manager.isEnabled() }.shuffled()
        if(enabledManagersList.isEmpty()) return null

        // we keep track of the amount that has eben generated already,
        // sometimes there is just not enough in 1 pool and thus there is more to generate in one of the next pools
        // besides that it also prevent rounding down errors
        // (an error that we prevent is: 10 / 3 = 3.3333, which would result in all thing generation 3, which would be 9 )
        var generationAMount = amount
        var tasks : Array<ITask> = emptyArray()
        for(i in enabledManagersList.indices) {
            val generationRatio = calculateRatioAmount( generationAMount / enabledManagersList.size - i, easyRatio, normalRatio, hardRatio)
            val partA = tasks
            val partB = enabledManagersList[i].generateTasks(associatedTeam, generationRatio, skip)
            if(partB != null){
                generationAMount -= partB.size
                tasks = unSaveCombineTasks(partA, partB)
            }
        }
        return tasks
    }

}