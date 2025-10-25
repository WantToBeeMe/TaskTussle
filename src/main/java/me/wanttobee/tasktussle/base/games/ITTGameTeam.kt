package me.wanttobee.tasktussle.base.games

import me.wanttobee.tasktussle.teams.ITeamObserver
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.entity.Player

// TODO: find a different place to initialize the starting progression, its a bit ugly here, but it is related to the team

// IMPORTANT: To prevent any annoying generic type cycles, we don't care about the generics in the gameTeam
abstract class ITTGameTeam(val associatedTeam: Team, startingProgression: String) : ITeamObserver {
    var associatedCard : ITTCardLogic<*>? = null
    val teamIcon = TeamIcon(this, associatedTeam, startingProgression)

    fun setAssociatedCard(card: ITTCardLogic<*>) : ITTGameTeam{
        if (associatedCard != card){
            associatedCard = card
            card.associatedGameTeams.add(this)
        }
        return this
    }

    init{
        associatedTeam.subscribe(this)
    }

    abstract fun clear()

    fun openCard() { associatedTeam.forEachMember { openCard(it) } }
    fun openCard(player: Player){ associatedCard?.openCard(player) }

    override fun onTeamClear() {
        teamIcon.clear()
        associatedCard?.clear()
        clear()
    }
    override fun onAddMember(member: Player) { teamIcon.refresh() }
    override fun onRemoveMember(member: Player) { teamIcon.refresh() }
    override fun onSwapMember(leave: Player, enter: Player) { teamIcon.refresh() }

    // Method delegations to the team
    fun containsMember(player: Player) : Boolean { return associatedTeam.containsMember(player) }
    fun forEachMember(action: (Player) -> Unit) { associatedTeam.forEachMember(action) }
}
