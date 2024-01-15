package me.wanttobee.tasktussle.teams

import org.bukkit.ChatColor
import org.bukkit.entity.Player

class Team(private val teamNumber : Int) {
    private val members : MutableList<Player> = mutableListOf()
    private val observers : MutableList<ITeamObserver> = mutableListOf()

    var leaveTeamOnQuit = false
        private set
    var color : ChatColor
        private set
    var leaveTeamOnDeath = false
        private set

    fun setLeaveTeamOnQuit(value : Boolean) : Team {
        this.leaveTeamOnQuit = value
        return this
    }
    fun setLeaveTeamOnDeath(value : Boolean) : Team {
        this.leaveTeamOnDeath = value
        return this
    }
    fun setColor(value : ChatColor) : Team {
        color = value
        return this
    }

    init{
        val defaultColors = arrayOf(ChatColor.BLUE, ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.LIGHT_PURPLE, ChatColor.GOLD, ChatColor.AQUA, ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE, ChatColor.DARK_AQUA)
        this.color = if(teamNumber <= 0) ChatColor.WHITE else defaultColors[(teamNumber-1)%10]
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

    fun addMember(players:Collection<Player>){
        for(p in players)
            this.addMember(p)
    }
    fun swapPlayer(leave:Player,enter:Player){
        if(!members.contains(leave) || members.contains(enter)) return
        //TeamSystem.minecraftPlugin.logger.info("swap begin")
        members.remove(leave)
        members.add(enter)

        for(ob in observers.toList())
            ob.onSwapMember(leave, enter)
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
            //p.setDisplayName(p.name)//resetting the name (so any color gets removed)
            for(ob in observers.toList())
                ob.onRemoveMember(p)
        }
        return done
    }
    fun containsMember(p : Player) : Boolean{
        return members.contains(p)
    }
    fun clear() {
        members.forEach { member ->
            member.setDisplayName(member.name)
        }
        members.clear()
        for(ob in observers)
            ob.onTeamClear()
        observers.clear()
    }

    fun applyToMembers( thing : (Player) -> Unit ){
        for(member in members){
            thing.invoke(member)
        }
    }

    fun getMemberString() : String{
        var text = ""
        for(member in members){
            text += member.name
            if(member != members.last()){
                text += ", "
            }
        }
        return text
    }
    fun getDisplayName() : String{
        return "${color}Team $teamNumber"
    }
    override fun toString(): String {
        return "${ChatColor.GOLD}Team $color-=$teamNumber=-${ChatColor.RESET}: ${getMemberString()} "
    }
}
