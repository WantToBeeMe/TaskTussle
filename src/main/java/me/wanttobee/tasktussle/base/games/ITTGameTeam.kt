package me.wanttobee.tasktussle.base.games

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.teams.ITeamObserver
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// TODO: find a different place to initialize the starting progression, its a bit ugly here, but it is related to the team

// IMPORTANT: To prevent any annoying generic type cycles, we don't care about the generics in the gameTeam
abstract class ITTGameTeam(val associatedTeam: Team, startingProgression: String) : ITeamObserver {
    var associatedCard : ITTCardLogic<*>? = null
    val teamIcon = TeamIcon(this, associatedTeam, startingProgression)
    protected val completedTasks : MutableSet<ITask> = mutableSetOf()

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

    fun onTaskCompleted(task: ITask){
        if (completedTasks.contains(task)){
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITTGameTeam) ERROR: Task is registered as complete multiple times (team: ${associatedTeam.getDisplayName()}/ task: ${task.icon.taskTitle})")
            return
        }

        completedTasks.add(task)
        updateProgression()
        sendCompletionMessage(task)
    }

    fun onTaskRevoked(task: ITask){
        if (!completedTasks.contains(task)){
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITTGameTeam) ERROR: Task completion is revoked while it was not registered as completed (team: ${associatedTeam.getDisplayName()} / task: ${task.icon.taskTitle})")
            return
        }

        completedTasks.remove(task)
        updateProgression()
    }

    fun updateProgression(){
        teamIcon.updateProgression("${completedTasks.size}/${associatedCard?.getTaskCount() ?: 0}", completedTasks.size)
    }

    private fun sendCompletionMessage(task: ITask){
        if(associatedCard == null){
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/ITTGameTeam) ERROR: Cannot update progression, no associated card")
            return
        }
        val teamSet = associatedCard!!.associatedSet

        if(TaskTussleSystem.cardVisibility != "visible"){
            teamSet.forEachTeam{ team ->
                team.forEachMember { p ->
                    if( associatedTeam.containsMember(p) )
                        p.sendMessage( task.getSuccessMessage(associatedTeam) )
                    else if( TaskTussleSystem.cardVisibility != "hidden" )
                        p.sendMessage("${associatedTeam.getDisplayName()}${ChatColor.RESET} got a task")
                }
            }
        } else {
            teamSet.broadcast( task.getSuccessMessage(associatedTeam) )
        }
    }

    open fun onGameFinished(){
        // This list is a list for all teh contributions to your team.
        // its "<member> = <percentage>%"
        val newContributionLore : MutableList<String> = mutableListOf()
        forEachMember { member ->
            val thisParticipation = completedTasks.count { task -> task.contributors.contains(member.name) }
            val percentage = ((thisParticipation/completedTasks.size.toFloat()) * 10000).toInt().toFloat() / 100
            newContributionLore += "${ChatColor.GRAY}${member.name} = $percentage%"
        }
        teamIcon.finishIcon(newContributionLore)
    }
}
