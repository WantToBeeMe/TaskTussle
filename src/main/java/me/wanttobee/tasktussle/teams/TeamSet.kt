package me.wanttobee.tasktussle.teams

import org.bukkit.ChatColor
import org.bukkit.entity.Player

// a teamSet is a set with a bunch of teams. the TeamSet has a specific type which all teams should match
// this type is the object that each team has
// for example this object can be a single number (Int) which would represent the score of that team
// however, it also can be a lot more complicated like some kind of BingoCard, where each time has 1 bingo card assigned to it
// In other words, the definition of a team is a list of players, and 1 object shared across these team members
class TeamSet<T>(private val teamObjectInitializer : (Team) -> T, private val title : String = "") {
    private val teams : MutableMap<Team, T> = mutableMapOf()
    private val observers : MutableList<ITeamSetObserver> = mutableListOf()


    init{
        TeamSystem.addTeamSet(this)
    }

    fun getTeamsAsMap() : MutableMap<Team, T>{
        return teams
    }

    fun subscribe(sub : ITeamSetObserver){
        if(!observers.contains(sub))
            observers.add(sub)
    }
    fun unSubscribe(sub : ITeamSetObserver){
        observers.remove(sub)
    }
    fun getTeamCount() : Int{
        return teams.size
    }

    // clears everything that has to be cleared before the set can be deleted
    fun clear(){
        // we first clear the set from the system
        TeamSystem.removeTeamSet(this)
        // then we tell all the teams that they are being cleared
        // so if they have observers of their own, they can pass the message
        for(team in teams.keys)
            team.clear()
        teams.clear() // we don't have to do it, but it just in case something goes wrong

        // and only then do we tell the observers of our own that we cleared
        for(ob in observers)
            ob.onSetClear()
        observers.clear() // we don't have to do it, but it just in case something goes wrong
    }

    // set all the teams setting to `leave team on quit`
    fun setLeaveOnQuit(value : Boolean){
        for(team in teams.keys){
            team.setLeaveTeamOnQuit(value)
        }
    }
    // set all the teams setting to `leave team on death`
    fun setLeaveOnDeath(value: Boolean){
        for(team in teams.keys){
            team.setLeaveTeamOnDeath(value)
        }
    }


    fun addTeam(team : Team, value : T){
        teams[team] = value
    }
    fun addTeam(team: Team) {
        return addTeam(team, teamObjectInitializer.invoke(team))
    }

    fun getTeam(player: Player): Team? {
        for(team in teams.keys){
            if (team.containsMember(player))
                return team
        }
        return null
    }
    fun getTeam(value: T): Team? {
        return teams.entries.find { it.value == value }?.key
    }


    // get the object for the given team
    fun getObject(team: Team): T? {
        return teams[team]
    }
    fun getObject(player: Player): T? {
        // gets the object for the given player,
        // so it first finds the team that it is in, and then it will find the object for that team
        for ((team, teamObject) in teams) {
            if (team.containsMember(player))
                return teamObject
        }
        return null
    }

    fun forEach(action : (Team, T)->Unit){
        for (team in teams)
            action.invoke(team.key, team.value)
    }
    fun forEachTeam(action : (Team)->Unit){
        for (team in teams.keys)
            action.invoke(team)
    }
    fun forEachObject(action : (T)->Unit){
        for (obj in teams.values)
            action.invoke(obj)
    }
    fun forEachPlayer(effect: (Player) -> Unit){
        for(team in teams.keys){
            for(member in team.getMembers())
                effect.invoke(member)
        }
    }

    // if this set contains this team
    fun containsTeam(team: Team): Boolean {
        return teams.containsKey(team)
    }
    // if this set contains this player
    fun containsPlayer(player: Player): Boolean {
        for(team in teams.keys){
            if (team.containsMember(player))
                return true
        }
        return false
    }

    // when a player leaves and the team setting leaveTeamOnQuit has been checked
    // than we will remove that player from the members
    // in the TeamSystem object it will still be added to the player leave list, but that doesn't matter because it is already out of the team itself.
    fun onPlayerLeave(player : Player){
        for(team in teams.keys){
            if(team.containsMember(player) && team.leaveTeamOnQuit)
                team.removeMember(player)
        }
    }
    // same as above, but than for when a player dies instead of leaves
    fun onPlayerDeath(player: Player){
        for(team in teams.keys){
            if(team.containsMember(player) && team.leaveTeamOnDeath )
                team.removeMember(player)
        }
    }

    fun onPlayerJoin(leavingPlayer : Player, joiningPlayer : Player){
        for(team in teams.keys){
            if(team.containsMember(leavingPlayer))
                team.swapPlayer(leavingPlayer, joiningPlayer)
        }
    }

    // sendMessage but for everyone in this set
    fun broadcast(message : String){
        for(team in teams.keys){
            for(member in team.getMembers())
                member.sendMessage(message)
        }
    }

    override fun toString(): String {
        var stringBuffer = title
        var first = true
        for((team,value) in teams){
            if(first){
                first = false
                var valueName = value!!::class.simpleName ?: "null"
                if(valueName == "Unit") valueName = "-"
                stringBuffer += "${ChatColor.GRAY} ($valueName)"
            }
            stringBuffer += "\n${ChatColor.WHITE}  - $team"
        }

        return stringBuffer
    }
}