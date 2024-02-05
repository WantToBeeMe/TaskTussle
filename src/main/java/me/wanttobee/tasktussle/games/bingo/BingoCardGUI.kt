package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.tasktussle.generic.cards.ITTCardGUI
import me.wanttobee.tasktussle.generic.cards.ITTCard
import me.wanttobee.tasktussle.generic.cards.TeamIcon
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet

class BingoCardGUI(associatedTeam : Team) : ITTCardGUI(associatedTeam, 25,15,45,"${associatedTeam.getDisplayName()} - Bingo") {
    override fun displayTask(tasks: Array<ITask>) : Boolean {
        if(tasks.size != taskAmount) return false
        val teamSetSize = associatedTeam.getSet()?.size ?: 1
        val pushOver = if(teamSetSize <= 10) 2 else 4
        for(i in 0 until taskAmount)
            tasks[i].icon.addToInventory(pushOver + (i%5) + 9*(i/5), this)
        //this.inventory.setItem(2 + (i%5) + 9*(i/5), tasks[i].icon.item )
        return true
    }

    override fun displayStatic() {
        fillGapsWithSeparator()
    }

    override fun <T : ITTCard> displayTeams(teams: TeamSet<T>) {
        val teamsMutableMap = teams.getTeamsAsMap().toList()
        if(teamsMutableMap.size > teamCount) return // its not possible then

        val teamLoop = if(teams.size <= 10) 10 else 15
        for(index in 0 until teamLoop){
            val spot =
                if(teams.size <= 10) 9*(index%5) + 8*(index/5)
                else 9*(index/3) + (index%3)
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
