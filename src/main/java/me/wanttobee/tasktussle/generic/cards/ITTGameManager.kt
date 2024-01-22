package me.wanttobee.tasktussle.generic.cards

import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import me.wanttobee.tasktussle.teams.TeamSystem
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// TaskTussle GAME MANAGER:
//  The system object / singleton / manager  of the specified game (lets say for example bingo)
//  This is the hub where everything related to bingo comes together
//  Requests from the outside come here and will be handled, or changed on the inside will be handled
interface ITTGameManager <T : ITTCard> {
    // if there is a no game active, this set is null
    var gameTeams : TeamSet<T>?
    // to make sure that whenever we start game, we have the default already predefined, and you don't have to anymore
    val defaultValue : ((Team) -> T)
    val teamRange : IntRange

    // this method will eventually call startGame(commander, gameSet)
    // when the game has been made
    fun startGame(commander: Player, teamAmount : Int) : Boolean{
        if(teamAmount !in teamRange){
            commander.sendMessage("${TaskTussleSystem.title} ${ChatColor.RED}team amount invalid, this game can have from ${teamRange.first} up to ${teamRange.last} teams total")
            return false
        }
        if(gameTeams != null)  {
            commander.sendMessage("${TaskTussleSystem.title} ${ChatColor.RED}this game is already running")
            return false
        }
        val playerCount = TaskTussleSystem.minecraftPlugin.server.onlinePlayers.size
        if(teamAmount > playerCount && !TaskTussleSystem.ignoreTeamSize) {
            commander.sendMessage("${TaskTussleSystem.title} ${ChatColor.RED}there are not enough players online to make${ChatColor.GRAY } $teamAmount ${ChatColor.RED}teams")
            return false
        }
        // from here we check what can go wrong, and it will go good from here, so we can make the teams
        if(TaskTussleSystem.choseTeamsBeforehand){
            // wer start the teams maker if we want to make the teams
            TeamSystem.startTeamMaker(commander,defaultValue,teamAmount, "Bingo") { set ->
                gameTeams = set
                set.forEachObject { cardManager -> cardManager.card.teamIcon.setClickable(!TaskTussleSystem.hideCard) }
                set.forEachPlayer { player -> TaskTussleSystem.clickItem.giveToPlayer(player) }
                startGame(commander,set) }
        }
        else {
            // otherwise we generate the teams randomly
            gameTeams = TeamSystem.generateTeams(teamAmount, "Bingo", defaultValue)
            gameTeams!!.forEachObject{ cardManager -> cardManager.card.teamIcon.setClickable(!TaskTussleSystem.hideCard) }
            gameTeams!!.forEachPlayer { player -> TaskTussleSystem.clickItem.giveToPlayer(player) }
            startGame(commander, gameTeams!!)
        }
        return true
    }

    // this startGame version is called when the important things are already assigned,
    // so you only have to override this and do the last specific game related things
    fun startGame(commander: Player, teams: TeamSet<T>)

    // this method will be called whenever a game finishes, the team that is put in as parameter is the winning team
    fun finishGame(winningTeam : Team)

    // this method ends the game, if this is called before the game is finished, the game will not have a winner :(
    open fun endGame() : Boolean{
        if(gameTeams == null) return false
        gameTeams!!.forEachObject { cardManager -> cardManager.card.clear() }
        gameTeams!!.clear()
        gameTeams = null
        return true
    }

    open fun debugStatus(commander: Player) {}
}