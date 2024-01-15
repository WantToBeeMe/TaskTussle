package me.wanttobee.tasktussle.utils.teams

import me.wanttobee.tasktussle.teams.ITeamSetObserver
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSystem
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// a teamSet is a set with a bunch of teams. the TeamSet has a specific type which all teams should match
// this type is the object that each team has
// for example this object can be a single number (Int) which would represent the score of that team
// however, it also can be a lot more complicated like some kind of BingoCard, where each time has 1 bingo card assigned to it
// In other words, the definition of a team is a list of players, and 1 object shared across these team members
class TeamSet<T>(private val defaultValue : (Team) -> T, private val title : String = "") {
//private val entityClass : Class<T>,
    private val teams : MutableMap<Team, T> = mutableMapOf()
    private val observers : MutableList<ITeamSetObserver> = mutableListOf()
    fun getSize() : Int{
        return teams.size
    }
    fun subscribe(sub : ITeamSetObserver){
        if(!observers.contains(sub))
            observers.add(sub)
    }
    fun unSubscribe(sub : ITeamSetObserver){
        observers.remove(sub)
    }
    fun toPairList() : List<Pair<Team, T>>{
        val returnList = mutableListOf<Pair<Team, T>>()
        for(team in teams)
            returnList.add(Pair(team.key,team.value))
        return returnList
    }


    init{
        TeamSystem.addTeamSet(this)
    }
    fun clear(){
        for(ob in observers)
            ob.onSetClear()
        TeamSystem.removeTeamSet(this)
    }

    fun setLeaveOnQuit(value : Boolean){
        for((team,_) in teams){
            team.setLeaveTeamOnQuit(value)
        }
    }
    fun setLeaveOnDeath(value: Boolean){
        for((team,_) in teams){
            team.setLeaveTeamOnDeath(value)
        }
    }

    fun addTeam(team : Team, value : T){
        teams[team]= value
    }
    fun addTeam(team: Team) {
        return addTeam(team, defaultValue.invoke(team))
    }

    fun getTeam(player: Player): Team? {
        for ((team, _) in teams) {
            if (team.containsMember(player))
                return team
        }
        return null
    }

    fun getTeam(value: T): Team? {
        return teams.entries.find { it.value == value }?.key
    }

    fun getT(team: Team): T? {
        return teams[team]
    }
    fun getT(player: Player): T? {
        for ((team, value) in teams) {
            if (team.containsMember(player))
                return value
        }
        return null
    }

    fun containsTeam(team: Team): Boolean {
        return teams.containsKey(team)
    }
    fun containsPlayer(player: Player): Boolean {
        for ((team, _) in teams) {
            if (team.containsMember(player))
                return true
        }
        return false
    }

    override fun toString(): String {
        var stringBuffer = "${ChatColor.GOLD}Set${ChatColor.RESET}: $title"
        var first = true
        for((team,value) in teams){
            if(first){
                first = false
                var valueName = value!!::class.simpleName ?: "null"
                if(valueName == "Unit") valueName = "-"
                stringBuffer += "${ChatColor.GRAY} ($valueName)"
            }
            stringBuffer += "\n${ChatColor.WHITE}- $team"
        }

        return stringBuffer
    }

    fun onPlayerLeave(player : Player){
        for(team in teams.keys){
            if(team.containsMember(player) && team.leaveTeamOnQuit)
                team.removeMember(player)
        }
    }

    fun onPlayerJoin(leftPlayer : Player, newPlayer : Player){
        for(team in teams.keys){
            if(team.containsMember(leftPlayer))
                team.swapPlayer(leftPlayer, newPlayer)
        }
    }
    fun onPlayerDeath(player: Player){
        for((team,_) in teams){
            if(team.leaveTeamOnDeath && team.containsMember(player))
                team.removeMember(player)
        }
    }

    fun broadcast(message : String){
        for((team, _) in teams){
            for(member in team.getMembers())
                member.sendMessage(message)
        }
    }
    fun applyToTeams( effect: (Team, T) -> Unit ){
        for((team,T) in teams)
            effect.invoke(team,T)
    }
    fun applyToAllMembers(effect: (Player) -> Unit){
        for((team,_) in teams){
            for(member in team.getMembers())
                effect.invoke(member)
        }
    }
    fun applyToOwnT(player : Player , effect : T.( ) -> Unit){
        for((team,T) in teams){
            if(team.containsMember(player)){
                T.effect()
                return
            }
        }
    }
}