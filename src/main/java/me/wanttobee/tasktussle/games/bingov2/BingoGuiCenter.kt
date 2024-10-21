package me.wanttobee.tasktussle.games.bingov2

import me.wanttobee.tasktussle.base.cards.ITTCardGUI
import me.wanttobee.tasktussle.base.games.ITTCardGui
import me.wanttobee.tasktussle.base.games.ITTGameTeam
import me.wanttobee.tasktussle.teams.TeamSet

class BingoGuiCenter(title: String) : ITTCardGui(45,title) {
    init{
        fillGapsWithSeparator()
    }

    fun <T: ITTGameTeam> setTeams(teams : TeamSet<T>){
        val teamsMutableMap = teams.getTeamsAsMap().toList()

        for(index in 0 until 10){
            val spot = 9*(index%5) + 8*(index/5)
            if(index >= teamsMutableMap.size)
                addLockedItem( spot, ITTCardGUI.emptyTeamIcon)
            else {
                teamsMutableMap[index]
                    .second.teamIcon
                    .addToInventory(spot,this)
            }
        }
    }
}