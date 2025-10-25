package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.tasktussle.base.games.ITTCardGui
import me.wanttobee.tasktussle.base.games.ITTGameTeam
import me.wanttobee.tasktussle.base.tasks.TaskIcon
import me.wanttobee.tasktussle.teams.TeamSet


// This class is essentially the View. It should NOT be responsible for anything outside the UI
// it just decides how to view things and that's it.
// things like clearing this card based on the game should happen outside this class, something else should invoke it.

// This class is called Center because there are going to be multiple variants of the Bingo GUI. also one
// where the bingo card is off center so that we can fit more teams.
class BingoGuiCenter(title: String) : ITTCardGui(45,title) {
    init{
        fillGapsWithSeparator()
    }

    fun <T: ITTGameTeam> setTeams(teams : TeamSet<T>){
        val teamsMutableMap = teams.getTeamsAsMap().toList()

        for(index in 0 until 10){
            val spot = 9*(index%5) + 8*(index/5)
            if(index >= teamsMutableMap.size)
                addLockedItem( spot, emptyTeamIcon)
            else {
                teamsMutableMap[index]
                    .second.teamIcon
                    .addToInventory(spot,this)
            }
        }
    }

    override fun displayTask(taskIconSet: Array<TaskIcon>) {
        val pushOver = 2 // How much each task should be pushed over to the right
        // (2 because 1 row is the teams, the other row are the separators)
        for(i in 0 until 25) // 25 because bingo always has 25 tasks
            taskIconSet[i].addToInventory(pushOver + (i%5) + 9*(i/5), this)
    }
}