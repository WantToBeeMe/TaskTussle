package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.generic.cards.ITTCardGUI
import me.wanttobee.tasktussle.generic.cards.ITTCard
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.generic.tasks.TaskState
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor

class BingoCard(private val associatedTeam : Team) : ITTCard {
    override val card: ITTCardGUI = BingoCardGUI(associatedTeam)
    private lateinit var taskSet : Array<ITask>

    // we have to save the tasks here because we want to be aware whenever a task is completed or anything
    override fun setTasks(tasks: Array<ITask>): Boolean {
        taskSet = tasks
        for (task in taskSet)
            task.setActive(this)
        return card.displayTask(taskSet)
    }

    // here we have no important buissness with the teams, however, we do need to inform the card itself to display it
    override fun <T : ITTCard> setTeams(teams: TeamSet<T>) {
        card.displayTeams(teams)
    }

    override fun onTaskDisabled(task: ITask) {
        if(task.stateCode != TaskState.COMPLETED){
            associatedTeam.forEachMember { p ->
                p.sendMessage("${TaskTussleSystem.title} ${ChatColor.RED} a wrong disableCode (${task.stateCode}) has been given in your card")
            }
            return
        }
        // TODO: Make per task a complete message
        // if(TaskTussleSystem.hideCard){
        //     BingoGameSystem.gameTeams?.forEach{ team,bingoCardManager ->
        //         if(bingoCardManager == this)
        //             team.forEachMember { p -> p.sendMessage("\"${associatedTeam.getDisplayName()}${ChatColor.RESET} got a task ${ChatColor.DARK_GRAY}(${ItemUtil.getRealName(task.itemToObtain)})") }
        //         else
        //             team.forEachMember { p -> p.sendMessage("\"${associatedTeam.getDisplayName()}${ChatColor.RESET} got a task")}
        //     }
        // }
        // else{
        //     BingoGameSystem.game?.applyToAllMembers { p ->  p.sendMessage("${associatedTeam.getDisplayName()}${ChatColor.RESET} got task: ${ChatColor.GOLD}${TTUtil.getRealName(task.itemToObtain)}")  }
        // }
        card.teamIcon.setAmount(getCompletedAmount())
        BingoGameManager.checkCardForWin(this)
    }

    fun getCompletedAmount() : Int {
        var value = 0
        for(t in taskSet)
            if(t.stateCode == TaskState.COMPLETED) value++
        return value
    }

    //this method returns triple<Horizontal, Vertical, Diagonal>
    fun getCompletedLines() : Triple<Int,Int,Int> {
        var horizontal = 0
        var vertical = 0
        var diagonal = 0

        var diagonalBool1 = true
        var diagonalBool2 = true
        for(i in 0 until 5){
            if( taskSet[0+i*5].stateCode == TaskState.COMPLETED &&
                taskSet[1+i*5].stateCode == TaskState.COMPLETED &&
                taskSet[2+i*5].stateCode == TaskState.COMPLETED &&
                taskSet[3+i*5].stateCode == TaskState.COMPLETED &&
                taskSet[4+i*5].stateCode == TaskState.COMPLETED )
                horizontal++

            if( taskSet[i+0*5].stateCode == TaskState.COMPLETED &&
                taskSet[i+1*5].stateCode == TaskState.COMPLETED &&
                taskSet[i+2*5].stateCode == TaskState.COMPLETED &&
                taskSet[i+3*5].stateCode == TaskState.COMPLETED &&
                taskSet[i+4*5].stateCode == TaskState.COMPLETED)
                vertical++

            if(taskSet[i + i*5].stateCode != TaskState.COMPLETED) diagonalBool1 = false
            if(taskSet[(4-i) + i*5].stateCode != TaskState.COMPLETED) diagonalBool2 = false
        }
        if(diagonalBool1) diagonal++
        if(diagonalBool2) diagonal++
        return Triple(horizontal,vertical,diagonal)
    }
}
