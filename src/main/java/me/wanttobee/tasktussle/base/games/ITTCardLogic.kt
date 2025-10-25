package me.wanttobee.tasktussle.base.games

import me.wanttobee.tasktussle.MinecraftPlugin
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.entity.Player

// to make the task themselves be dynamic, we need to make it so that cards request the tasks from the game manager (and not the other way around)
abstract class ITTCardLogic<T: ITTGameTeam>(val associatedSet: TeamSet<T>) {
    abstract var cardGui : ITTCardGui?
    val associatedGameTeams : MutableList<ITTGameTeam> = mutableListOf()
    protected lateinit var taskSet : Array<ITask>

    fun openCard(player : Player){ cardGui?.open(player) }

    // if you don't allow any skip tokens in a sustain game mode,
    // make sure you init it to 0,  otherwise init it to the amount in the settings
    //    override var skipTokens: Int = 0
    //    override var successTokens: Int = TaskTussleSystem.succeedTokens
    abstract var skipTokens : Int
    abstract var successTokens : Int
    abstract val skipTokensMax : Int
    abstract val successTokensMax : Int

    abstract fun onTaskDisabled(task : ITask)
    open fun onTaskEnabled(task : ITask) {}
    // you know that T is the gameTeam of the game that this logic card is associated with. so if you really need that type context you can just do `teams as TeamSet<...>`
    abstract fun selectCardGui()

    fun setTasks(newSet: Array<ITask>) {
        if (::taskSet.isInitialized){
            MinecraftPlugin.instance.logger.warning("(TaskTussle/ITTCardLogic) ERROR: Trying to set tasks for a card that already has tasks assigned. Make sure to only call `setTasks` once per card.")
            return
        }

        taskSet = newSet
        if (cardGui == null)
            MinecraftPlugin.instance.logger.warning("(TaskTussle/ITTCardLogic) ERROR: Trying to set tasks for a card that has not yet been selected. Make sure that the \"card Gui\" has been selected before calling `setTasks` from the \"card Logic\"")

        for (iTask in taskSet) {
            iTask.setOwnership(this)
        }
        cardGui?.displayTask(taskSet.map { it.icon }.toTypedArray())
    }

    open fun clear(){
        cardGui?.clear()
    }

    fun getTaskCount(): Int { return taskSet.size }
}