package me.wanttobee.tasktussle.teams

interface ITeamSetObserver {
    // this happens whenever the set is being deleted
    // a set will be deleted when a game ends for example, then the game is done and everything will be cleaned up
    fun onSetClear()
}
