package me.wanttobee.tasktussle.base.games

import me.wanttobee.tasktussle.teams.ITeamObserver
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.entity.Player

// TODO: find a different place to initialize the starting progression, its a bit ugly here, but it is related to the team

abstract class ITTGameTeam(val associatedTeam: Team, startingProgression: String ) : ITeamObserver {
    var associatedCard : ITTCardLogic? = null
    val teamIcon = TeamIcon(this, associatedTeam, startingProgression)

    fun setAssociatedCard(card: ITTCardLogic) : ITTGameTeam{
        associatedCard = card
        return this
    }


    init{
        associatedTeam.subscribe(this)
    }
    abstract fun clear()


    fun openCard(player: Player){ associatedCard?.openCard(player) }

    override fun onTeamClear() {
        teamIcon.clear()
        associatedCard?.cardGui?.clear()
        clear()
    }
    override fun onAddMember(member: Player) { teamIcon.refresh() }
    override fun onRemoveMember(member: Player) { teamIcon.refresh() }
    override fun onSwapMember(leave: Player, enter: Player) { teamIcon.refresh() }
}
