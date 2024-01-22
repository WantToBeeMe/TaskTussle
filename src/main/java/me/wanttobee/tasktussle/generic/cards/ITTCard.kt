package me.wanttobee.tasktussle.generic.cards

import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.entity.Player

// TaskTussle CARD:
//  The logic part of the card for your specified game (lets say for example bingo)
//  Your card has is build out of 2 parts, the Card and the CardGUI
//  (or more specific, each Card contains its own CardGUI)
//  anyway, the Card is in control of the logic and the CardGUI is just the interface
interface ITTCard {
    val card : ITTCardGUI
    fun openCard(player : Player){ card.open(player) }

    // this method is to make sure that taskCardManager knows what tasks are being used for this card
    // in the manager you might want to save it if this is being called, who knows
    // the manager can update the card on this call if needed
    fun setTasks(tasks : Array<ITask>) : Boolean

    // this method is to make sure the taskCardManager knows what teams they will be playing with
    // the manager can update the card on this call if needed
    fun <T:ITTCard> setTeams(teams : TeamSet<T>)

    fun onTaskDisabled(task : ITask)
}
