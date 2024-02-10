package me.wanttobee.tasktussle.teams

import org.bukkit.ChatColor
import org.bukkit.entity.Player

// teamIndex is the index of the team within the teamSet
// for example, you can be in `Team 1`, or in `Team 2`
class Team(val teamIndex : Int) {
    private val members : MutableList<Player> = mutableListOf()
    private val observers : MutableList<ITeamObserver> = mutableListOf()

    var leaveTeamOnQuit = false
        private set
    var color : ChatColor
        private set
    var leaveTeamOnDeath = false
        private set

    init{
        val defaultColors = arrayOf(ChatColor.BLUE, ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.AQUA, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_AQUA)
        this.color = if(teamIndex <= 0) ChatColor.WHITE else defaultColors[(teamIndex-1) % defaultColors.size]
    }

    fun setLeaveTeamOnQuit(value : Boolean) : Team {
        this.leaveTeamOnQuit = value
        return this
    }
    fun setLeaveTeamOnDeath(value : Boolean) : Team {
        this.leaveTeamOnDeath = value
        return this
    }
    // you can change the color to something specific if you want. but it is set to a color by default
    fun setColor(value : ChatColor) : Team {
        color = value
        return this
    }

    fun subscribe(ob : ITeamObserver){
        if(!observers.contains(ob))
            observers.add(ob)
    }
    fun unsubscribe(ob : ITeamObserver) : Boolean{
        return observers.remove(ob)
    }

    fun getMembers() : List<Player>{
        return members.toList()
    }

    fun containsMember(p : Player) : Boolean{
        return members.contains(p)
    }
    fun addMember(players:Collection<Player>){
        for(p in players)
            this.addMember(p)
    }
    fun addMember(p : Player){
        if(members.contains(p)) return
        members.add(p)
        for(ob in observers.toList()) //it's already a list, but the toList creates a clone, so in the can modify the observer list without messing with this current loop
            ob.onAddMember(p)
    }
    fun removeMember(p: Player) : Boolean{
        val done = members.remove(p)
        if(done){
            for(ob in observers.toList())
                ob.onRemoveMember(p)
        }
        return done
    }

    // this method will swap out the first player for the second player
    // you might have some kind of ability that does this in your game where this can be useful
    // we also use this for when players leave the server and rejoin, they then have a new player instance,
    // so we swap the old player instance for there new player instance
    fun swapPlayer(leave:Player, enter:Player){
        if(!members.contains(leave) || members.contains(enter)) return
        //TeamSystem.minecraftPlugin.logger.info("swap begin")
        members.remove(leave)
        members.add(enter)

        for(ob in observers.toList())
            ob.onSwapMember(leave, enter)
    }

    fun clear() {
       // we clear
        members.clear() // we don't have to do it, but it just in case something goes wrong.
        // and then we tell all the observers that we cleared
        for(ob in observers)
            ob.onTeamClear()
        observers.clear() // we don't have to do it, but it just in case something goes wrong
    }

    fun forEachMember(thing : (Player) -> Unit ){
        for(member in members){
            thing.invoke(member)
        }
    }

    fun getDisplayName() : String{
        return "${color}Team $teamIndex"
    }
    override fun toString(): String {
        return "${ChatColor.GOLD}Team $color-=$teamIndex=-${ChatColor.RESET}: ${members.joinToString(", ") { p -> p.name }} "
    }
    fun getSet() : TeamSet<*>?{
        return TeamSystem.findSetByTeam(this)
    }
}
