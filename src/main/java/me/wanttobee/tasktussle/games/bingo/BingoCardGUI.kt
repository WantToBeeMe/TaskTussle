package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.tasktussle.generic.cards.ITTCardGUI
import me.wanttobee.tasktussle.generic.cards.ITTCard
import me.wanttobee.tasktussle.generic.cards.TeamIcon
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet

class BingoCardGUI(associatedTeam : Team) : ITTCardGUI(associatedTeam, 25,10,45,"${associatedTeam.getDisplayName()} - Bingo") {
    override val teamIcon: TeamIcon = TeamIcon(this, associatedTeam, 25)

    override fun displayTask(tasks: Array<ITask>) : Boolean {
        if(tasks.size != 25) return false
        for(i in 0 until 25)
            tasks[i].icon.addToInventory(2 + (i%5) + 9*(i/5), this)
        //this.inventory.setItem(2 + (i%5) + 9*(i/5), tasks[i].icon.item )
        return true
    }

    override fun displayStatic() {
        fillGapsWithSeparator()
    }

    override fun <T : ITTCard> displayTeams(teams: TeamSet<T>) {
        val teamsMutableMap = teams.getTeamsAsMap().toList()
        if(teamsMutableMap.size > 10) return // its not possible then

        for(index in 0 until 10 ){
            val spot = 9*(index%5) + 8*(index/5)
            if(index >= teamsMutableMap.size)
                addLockedItem( spot, emptyTeamIcon )
            else {
                teamsMutableMap[index]
                    .second.card.teamIcon
                    .addToInventory(spot,this)
            }
        }
    }
}
