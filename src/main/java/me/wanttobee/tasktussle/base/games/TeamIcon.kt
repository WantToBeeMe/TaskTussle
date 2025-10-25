package me.wanttobee.tasktussle.base.games

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.util.toLore
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material

class TeamIcon(private val ownerInventory : ITTGameTeam, private val associatedTeam : Team, startingProgression: String){
    // we save the unique items here ,but we don't subscribe to them because we don't really care
    private val publicTeamIcon = UniqueItemStack(
        Material.WHITE_STAINED_GLASS.colorize(associatedTeam.color), associatedTeam.getDisplayName(), null)
    private val privateTeamIcon = UniqueItemStack(
        Material.WHITE_CONCRETE.colorize(associatedTeam.color), "${associatedTeam.color}This Card", null, true)

    private val disableClickEvent : MutableList<()->Unit> = mutableListOf()
    private val enableClickEvent : MutableList<()->Unit> = mutableListOf()
    // if the game is finished, then this is a list of the contributions
    // otherwise its null (meaning the game is not yet finished)
    private var gameFinished : Collection<String>? = null
    private var currentProgress = "0"
    private var currentProgressStack = 0
    // clickable indicates what the previous state was of this icon,
    // so we don't constantly update the inventories events when it's not needed
    private var currentlyClickable = false


    init{
        updateProgression(startingProgression, 0)
    }

    // add to any interactiveInventory
    fun addToInventory(slot: Int, inv : InteractiveInventory){
        // if the provided inventory is teams inventory, then it will do the privateTeamIcon
        if(inv == ownerInventory.associatedCard?.cardGui)
            inv.addLockedItem(slot,privateTeamIcon )
        // otherwise it is not the current inventory, and then it will do the public team icon
        else {
            // we add it to the inventory
            if(currentlyClickable)
                inv.addLockedItem(slot, publicTeamIcon){ player,_ -> ownerInventory.openCard(player) }
            else
                inv.addLockedItem(slot, publicTeamIcon)

            // we make sure that we can disable and enable the item
            // TODO:
            //  make the disabling and enabling more efficient, currently it will replace the item with itself, with or without the event
            //  we could make it so it will only replace the event
            disableClickEvent.add {
                inv.removeItem(publicTeamIcon)
                inv.addLockedItem(slot, publicTeamIcon,null)
            }
            enableClickEvent.add {
                inv.removeItem(publicTeamIcon)
                inv.addLockedItem(slot, publicTeamIcon) { player,_ ->
                    ownerInventory.openCard(player)
            } }
        }
    }

    fun updateProgression(progress : String, itemCount: Int){
        currentProgress = "${ChatColor.GRAY}$progress"
        currentProgressStack = itemCount
        refresh()
    }

    fun finishIcon(contributors: Collection<String>?){
        gameFinished = contributors
        refresh()
    }

    private fun updateClickable(visibility: String){
        val isClickable = visibility == "visible"
        if(currentlyClickable == isClickable) return
        // if it is the same as it was, we don't do anything
        // if not, then we have to update the clickable stuff
        if(isClickable) publicTeamIcon.updateMaterial(Material.WHITE_CONCRETE.colorize(associatedTeam.color)).pushUpdates()
        else publicTeamIcon.updateMaterial(Material.WHITE_STAINED_GLASS.colorize(associatedTeam.color)).pushUpdates()
        if(isClickable) for (enableEvent in enableClickEvent) { enableEvent.invoke() }
        else for (disableEvent in disableClickEvent) { disableEvent.invoke() }
        currentlyClickable = isClickable
    }

    // this recreates the teams visual under the item, can be useful whenever a team changes,
    // that you can make it refresh the list that it shows
    fun refresh(){
        // public is the icon that everyone sees
        // private is the icon that only your team sees (or if you are in another teams inventory)
        val privateLore : MutableList<String> = mutableListOf()
        var publicLore : MutableList<String> = mutableListOf()
        var visibility = TaskTussleSystem.cardVisibility
        if(gameFinished != null){
            // when the game is finished, we don't really care what the visibility setting is set to,
            // we always want it to be visible so each team can look at what the other team did
            visibility = "visible"
            privateLore += listOf(currentProgress) + gameFinished!!
            publicLore = privateLore
        } else{
            val memberString = "${ChatColor.GRAY}" + associatedTeam.getMembers().joinToString(", ") { p -> p.name }
            privateLore += memberString.toLore(35) + currentProgress
            publicLore += memberString.toLore(35)
            if(visibility != "hidden")
                publicLore += currentProgress
        }

        val privateAmount = currentProgressStack
        val publicAmount = if(visibility == "hidden") 0 else currentProgressStack

        publicTeamIcon
            .updateLore(publicLore)
            .updateCount(if(publicAmount < 1) 1 else publicAmount)
            .pushUpdates()
        privateTeamIcon
            .updateLore(privateLore)
            .updateCount(if(privateAmount < 1) 1 else privateAmount)
            .pushUpdates()

        updateClickable(visibility)
    }

    fun clear(){
        publicTeamIcon.clearItem()
        privateTeamIcon.clearItem()
    }
}
