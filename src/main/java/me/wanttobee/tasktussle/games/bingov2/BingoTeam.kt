package me.wanttobee.tasktussle.games.bingov2

import me.wanttobee.tasktussle.base.games.ITTCardLogic
import me.wanttobee.tasktussle.base.games.ITTGameTeam
import me.wanttobee.tasktussle.teams.Team

// because each team has it own card in bingo we can require it in the constructor already, but this might not be the case for other games
class BingoTeam(card: ITTCardLogic, associatedTeam: Team) : ITTGameTeam( associatedTeam, "0/25") {

    init{
        this.setAssociatedCard(card)
    }

    override fun clear() {}
}
