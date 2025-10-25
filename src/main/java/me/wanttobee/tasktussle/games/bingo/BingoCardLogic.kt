package me.wanttobee.tasktussle.games.bingo

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.games.ITTCardGui
import me.wanttobee.tasktussle.base.games.ITTCardLogic
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.base.tasks.TaskState
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor

// The card Logic is essentially the Model in a MVC architecture. It should NOT be responsible for anything UI related, that is the CardGui
// It is however responsible for what type of cardGui to use. Anyway, it should handle all the logic for 1 specific card.

class BingoCardLogic(associatedSet: TeamSet<BingoTeam>) : ITTCardLogic<BingoTeam>(associatedSet) {
    override var cardGui: ITTCardGui? = null

    override var skipTokens: Int = 0
    override var successTokens: Int = TaskTussleSystem.succeedTokens
    override val skipTokensMax: Int = skipTokens
    override val successTokensMax: Int = successTokens

    // FIXME: this method is unhinged. it should save the bingoCardLogic from having an hardcoded card. currently when teams share one card
    //  this method gets called multiple times on the same card.
    override fun selectCardGui() {
        val newCard = BingoGuiCenter("Bingo")
        cardGui = newCard
        newCard.setTeams(associatedSet)
    }

    override fun onTaskDisabled(task: ITask) {
        if(task.stateCode != TaskState.COMPLETED){
            for (associatedGameTeam in associatedGameTeams) {
                associatedGameTeam.forEachMember { p ->
                    // It's not necessarily wrong to get a different code than Complete.
                    // It is however wrong in bingo, since when playing bingo we don't expect any other code than Completed.
                    p.sendMessage("${TaskTussleSystem.title}${ChatColor.RED} a wrong disableCode (${task.stateCode}) has been given in your card")
                }
            }
            return
        }

        BingoManager.checkCardForWin(this)
    }

    //this method returns triple<Horizontal, Vertical, Diagonal>
    fun getCompletedLines() : Triple<Int, Int, Int> {
        // This method is used to determent whether the game has been won or not.
        // Here we only look at the tasks related to this card, regardless of the associated team. This is possible because single cards are not shared between teams.
        // But if this was not the case, we could not do it this way
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