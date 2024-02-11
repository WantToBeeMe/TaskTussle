package me.wanttobee.tasktussle.teams


import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.util.toLore
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.Inventory

class TeamMakerInventory(private val teamCount : Int, private val maxTeamSize : Int = 100 )  : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null,9 + 9 * (teamCount/9),"${ChatColor.GOLD}Team Maker")
    private lateinit var teams : Array<Team>

    init {
        if (maxTeamSize in 1..127){
            teams = Array(teamCount) { i -> Team(i+1) }
            refreshTeamVisualizer()
        }
    }

    // refreshes the inventories items when any changes happen (when someone clicks on another team to move for example)
    // this way the items stay up to date
    // TODO:
    //  implement the new way of updating the items, instead of refreshing the whole inventory when someone clicks an item,
    //  now only update the item that changed
    private fun refreshTeamVisualizer(){
        // loops through all the teams and create there corresponding button
        for(index in 0 until teamCount){
            val team = teams[index]
            val teamMembers = team.getMembers()
            var item: UniqueItemStack
            // sets item to glass if its empty
            if(teamMembers.isEmpty()){
                item = UniqueItemStack( Material.WHITE_STAINED_GLASS.colorize(team.color),
                    team.getDisplayName(),"${ChatColor.DARK_GRAY} Empty",1 )
            }
            // otherwise its sets it to its concrete variant with the member names
            else{
                var memberString = "${ChatColor.GRAY}"
                for(memberID in teamMembers.indices){
                    memberString += teamMembers[memberID].name
                    if(memberID != teamMembers.size -1)
                        memberString += ", "
                }
                item = UniqueItemStack( Material.WHITE_CONCRETE.colorize(team.color),
                        team.getDisplayName(), memberString.toLore(35),teamMembers.size , true)
            }

            // make button click which will add a player to this team and remove it from the once it was previously in
            this.addLockedItem(index, item) { player,_ ->
                val thisTeam = teams[index]
                if(thisTeam.containsMember(player))
                    thisTeam.removeMember(player)
                else {
                    for(t in teams)
                        t.removeMember(player)
                    thisTeam.addMember(player)
                }
                refreshTeamVisualizer()
            }
        }
    }

    // this returns a list with teams that was kept track of while running this inventory
    fun exportTeams() : Array<Team>{
        this.clear()
        return teams.filter { team -> team.getMembers().isNotEmpty() }.toTypedArray()
    }
}
