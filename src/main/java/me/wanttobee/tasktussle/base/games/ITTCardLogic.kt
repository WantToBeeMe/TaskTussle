package me.wanttobee.tasktussle.base.games

import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.games.bingov2.BingoTeam
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.entity.Player

// to make the task themselves be dynamic, we need to make it so that cards request the tasks from the game manager (and not the other way around)
interface ITTCardLogic {
    var cardGui : ITTCardGui?
    fun openCard(player : Player){ cardGui?.open(player) }

    // if you don't allow any skip tokens in a sustain game mode,
    // make sure you init it to 0,  otherwise init it to the amount in the settings
    //    override var skipTokens: Int = 0
    //    override var successTokens: Int = TaskTussleSystem.succeedTokens
    var skipTokens : Int
    var successTokens : Int
    val skipTokensMax : Int
    val successTokensMax : Int

    fun onTaskDisabled(task : ITask)
    // you know that T is the gameTeam of the game that this logic card is associated with. so if you really need that type context you can just do `teams as TeamSet<...>`
    fun <T: ITTGameTeam> selectCardGui(teams: TeamSet<T>)
}