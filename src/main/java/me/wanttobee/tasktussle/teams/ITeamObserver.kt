package me.wanttobee.tasktussle.teams

import org.bukkit.entity.Player

interface ITeamObserver {
    // this happens whenever the set is being deleted
    // a set will be deleted when a game ends for example, then the game is done and everything will be cleaned up
    fun onTeamClear()

    fun onAddMember(member : Player)
    fun onRemoveMember(member: Player)
    fun onSwapMember(leave: Player, enter:Player)
}