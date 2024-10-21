package me.wanttobee.tasktussle.games.bingov2

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.games.ITTCardGui
import me.wanttobee.tasktussle.base.games.ITTCardLogic
import me.wanttobee.tasktussle.base.games.ITTGameTeam
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskState
import me.wanttobee.tasktussle.teams.TeamSet

class BingoCardLogic : ITTCardLogic {
    override var cardGui: ITTCardGui? = null
    private lateinit var taskSet : Array<ITask>

    override var skipTokens: Int = 0
    override var successTokens: Int = TaskTussleSystem.succeedTokens
    override val skipTokensMax: Int = skipTokens
    override val successTokensMax: Int = successTokens

    // FIXME: this method is unhinged. it should save the bingoCardLogic from having an hardcoded card. currently when teams share one card
    //  this method gets called multiple times on the same card.
    override fun <T : ITTGameTeam> selectCardGui(teams: TeamSet<T>) {
        val newCard = BingoGuiCenter("Bingo")
        cardGui = newCard
        newCard.setTeams(teams)
    }

    override fun onTaskDisabled(task: ITask) {
        TODO("Not yet implemented")
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