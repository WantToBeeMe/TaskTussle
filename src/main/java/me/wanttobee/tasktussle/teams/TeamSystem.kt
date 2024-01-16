package me.wanttobee.tasktussle.teams

import me.wanttobee.everythingitems.ItemUtil
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

object TeamSystem: Listener {
    lateinit var minecraftPlugin : JavaPlugin
        private set
    var title : String? = null
        private set

    // a teamSet is a set with a bunch of teams. the TeamSet has a specific type which all teams should match
    // this type is the object that each team has
    // for example this object can be a single number (Int) which would represent the score of that team
    // however, it also can be a lot more complicated like some kind of BingoCard, where each time has 1 bingo card assigned to it
    // In other words, the definition of a team is a list of players, and 1 object shared across these team members
    private val activeTeamSets : MutableList<TeamSet<*>> = mutableListOf()
    fun addTeamSet(teamSet : TeamSet<*>){ activeTeamSets.add(teamSet) }
    fun removeTeamSet(teamSet : TeamSet<*>){ activeTeamSets.remove(teamSet) }

    // it should be initialized once the plugin starts to make sure everything works as it should
    fun initialize(plugin: JavaPlugin, title: String?) {
        minecraftPlugin = plugin
        TeamSystem.title = title
    }
    fun disablePlugin(){
        clearAll()
    }

    // clears all the teams that are currently exists
    fun clearAll(){
        for(amount in activeTeamSets.indices)
            activeTeamSets[0].clear()
    }

    fun listTeams(sender : Player){
        if(activeTeamSets.isEmpty()){
            sender.sendMessage("$title ${ChatColor.RED}There are no teams to list")
            return
        }
        sender.sendMessage("$title ${ChatColor.GREEN}Teams List: ")
        for(set in activeTeamSets)
            sender.sendMessage("${ChatColor.GOLD}}$set")
    }

    // a method that creates a set with teams of equal size with all the players that are online
    // The last parameter makes sure that each team has there designated team value
    fun <T> makeTeams(teamCount : Int, teamObjectInitializer : (Team) -> T) : TeamSet<T> {
        return makeTeams(teamCount, "Unknown Team Set", null, teamObjectInitializer)
    }
    fun <T> makeTeams(teamCount : Int, setTitle : String, teamObjectInitializer : (Team) -> T) : TeamSet<T> {
        return makeTeams(teamCount,setTitle, null, teamObjectInitializer )
    }
    fun <T> makeTeams(teamCount : Int, setTitle : String, preGeneratedValues : Array<T>?, teamObjectInitializer : (Team) -> T ) : TeamSet<T> {
        val onlinePlayers = minecraftPlugin.server.onlinePlayers.shuffled()
        val playerCount = onlinePlayers.size
        val teamSize = playerCount / teamCount // we try to make each team the same size
        val remainingPlayers = playerCount % teamCount // but it may just be that there will be some players remaining
        var currentPlayerIndex = 0
        val set = TeamSet(teamObjectInitializer,setTitle)
        for(i in 0 until teamCount){
            // the first n remaining players will be split up in the first n teams
            val size = teamSize + if (i < remainingPlayers) 1 else 0
            val team = Team(i+1)
            for (j in 0 until size) {
                team.addMember(onlinePlayers[currentPlayerIndex])
                currentPlayerIndex++
            }
            if(preGeneratedValues != null && preGeneratedValues.size > i)
                set.addTeam(team, preGeneratedValues[i])
            else
                set.addTeam(team)
        }
        return set
    }

    // the teamMaker is a menu which will allows everyone to choice there teams
    // the processStarter is the player who invoked the teamMaker menu
    // when something goes wrong while creating the teams, then this player will get the messages in its chat
    fun <T> teamMaker(processStarter : Player, teamObjectInitializer : (Team) -> T, teamCount: Int, title:String, preGeneratedValues : Array<T>?, effect: (TeamSet<T>) -> Unit) {
        return teamMaker(processStarter, teamObjectInitializer, teamCount,title,100,preGeneratedValues,effect)
    }
    fun <T> teamMaker(processStarter:Player, teamObjectInitializer : (Team) -> T, teamCount: Int, title:String, effect:(TeamSet<T>) -> Unit) {
        return teamMaker(processStarter,teamObjectInitializer, teamCount,title,100,null,effect)
    }
    fun <T> teamMaker(processStarter:Player, teamObjectInitializer : (Team) -> T, teamCount: Int, effect: (TeamSet<T>) -> Unit){
        return teamMaker(processStarter,teamObjectInitializer, teamCount, "Unknown Team Set", 100,null, effect)
    }
    fun <T> teamMaker(processStarter:Player, teamObjectInitializer : (Team) -> T, teamCount: Int, title : String, maxTeamSize : Int, preGeneratedValues : Array<T>?, effect : (TeamSet<T>) -> Unit){
        if(teamCount == 1){
            // when there is 1 team, there is no choice, so we will instantly create the teams
            processStarter.sendMessage("$title ${ChatColor.RED}with 1 team to chose from there isn't any choice, so it has been completed already")
            val team = Team(0+1)
            team.addMember(minecraftPlugin.server.onlinePlayers)
            val set = TeamSet<T>(teamObjectInitializer,title)
            if(!preGeneratedValues.isNullOrEmpty())
                set.addTeam(team, preGeneratedValues[0])
            else
                set.addTeam(team)
            effect.invoke(set)
            return
        }
        // if the numbers are not possible, we make sure to tell the proccess starter
        if(teamCount <= 1){
            processStarter.sendMessage("$title ${ChatColor.RED}the team count has to be a number above 1")
            return
        }
        if(maxTeamSize < 1){
            processStarter.sendMessage("$title ${ChatColor.RED}it isn't really fun to play with empty teams, teamSize has to be 1 or above")
            return
        }
        if(teamCount > 54){
            processStarter.sendMessage("$title ${ChatColor.RED}due to minecraft inventory limit its not possible to create a visualizer for this, you will have to let them be random generated for you (but what do you need all these teams for anyway)")
            return
        }
        if(maxTeamSize > 127){
            processStarter.sendMessage("$title ${ChatColor.RED}due to minecraft stack limit its not possible to create a visualizer for this, you will have to let them be random generated for you (but what do you need such big teams anyway)")
            return
        }

        // from here the number validation is out of the way, and we can start creating the menu which allows us to select our teams
        val teamMakerInventory = TeamMakerInventory(teamCount,maxTeamSize)

        // we now give each player an item which they can click on to open this teamMaker menu
        // if they close the menu that's no problem, they can reopen it again as many times as they want
        val teamMakerItem = InteractiveHotBarItem()
            .setSlot(8)
            .setItem(ItemUtil.itemFactory(Material.TNT, "${ChatColor.RED}Team ${ChatColor.WHITE}Navigator ${ChatColor.RED}Tool", "${ChatColor.GRAY}Use this item to chose your team", true))
            .setRightClickEvent { player,_ -> teamMakerInventory.open(player) }
        for(p in minecraftPlugin.server.onlinePlayers) {
            teamMakerItem.giveToPlayer(p)
        }

        // we will create an item which allows the process starter to also finish this team creation
        // this item will only be given to the player that started this process after all
        val teamFinisher = InteractiveHotBarItem()
           .setSlot(7)
           .setItem(ItemUtil.itemFactory(Material.FIRE_CHARGE, "${ChatColor.GOLD}Detonate Current Teams", "${ChatColor.GRAY}Use these teams", true))
        teamFinisher.setRightClickEvent { _,_ ->
            teamMakerItem.clear()
            // we extract the teams from the teamMaker
            val teams = teamMakerInventory.exportTeams()
            val set = TeamSet(teamObjectInitializer,title)
            // and we initialize the set with their teams
            for(teamIndex in teams.indices){
                if(preGeneratedValues != null && preGeneratedValues.size > teamIndex )
                    set.addTeam(teams[teamIndex], preGeneratedValues[teamIndex])
                else set.addTeam(teams[teamIndex])
            }
            effect.invoke(set)
            teamFinisher.clear()
        }
        // note that this line happens before the setRightClickEvent you see above
        teamFinisher.giveToPlayer(processStarter)
    }

    @EventHandler
    fun onPlayerDeath(event : PlayerDeathEvent){
        val player = event.entity
        for(set in activeTeamSets)
            set.onPlayerDeath(player)
    }

    // if someone has an internet problem and they accedently leave, we don't want to ruin the teams
    // if they rejoin we still want them to be able to play again
    // so whe save the player if they leave
    // and once they rejoin again, we check this list if they are in it (by checking there uniqueID which should never change)
    // and if they are, we check in every team that contains the quited version of this player,
    // and we replace that quited player with this newly rejoined player (even though it is the same one, do you still follow?)
    private val quitPlayers : MutableList<Player> = mutableListOf()
    @EventHandler
    fun onPlayerLeave(event : PlayerQuitEvent){
        val player = event.player
        quitPlayers.add(player)
        for(set in activeTeamSets)
            set.onPlayerLeave(player)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent){
        val player = event.player
        for(quitPlayer in quitPlayers){
            if(player.uniqueId == quitPlayer.uniqueId){
                for(set in activeTeamSets){
                    if(set.containsPlayer(quitPlayer))
                        set.onPlayerJoin(quitPlayer, player)
                }
                quitPlayers.remove(quitPlayer)
                return
            }
        }
    }
}
