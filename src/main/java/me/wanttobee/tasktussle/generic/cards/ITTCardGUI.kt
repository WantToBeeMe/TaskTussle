package me.wanttobee.tasktussle.generic.cards

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.generic.tasks.ITask
import me.wanttobee.tasktussle.teams.ITeamObserver
import me.wanttobee.tasktussle.teams.Team
import me.wanttobee.tasktussle.teams.TeamSet
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory

// TaskTussle CARD GUI:
//  The interface part of the card for your specified game (lets say for example bingo)
//  Your card has is build out of 2 parts, the Card and the CardGUI
//  (or more specific, each Card contains its own CardGUI)
//  anyway, the Card is in control of the logic and the CardGUI is just the interface
abstract class ITTCardGUI(protected val associatedTeam : Team, val taskAmount : Int, val teamCount : Int, inventorySlotAmount : Int, inventoryTitle: String) : InteractiveInventory() , ITeamObserver {
    companion object{
        val emptyTeamIcon = UniqueItemStack(Material.GRAY_STAINED_GLASS, "${ChatColor.RESET}${ChatColor.GRAY}Empty Card", null)
    }
    open val teamIcon : TeamIcon  = TeamIcon(this, associatedTeam, taskAmount)
    override var inventory: Inventory = if(inventorySlotAmount == 5)
        Bukkit.createInventory(null, InventoryType.HOPPER, inventoryTitle)
        else Bukkit.createInventory(null, inventorySlotAmount, inventoryTitle)


    init{
        associatedTeam.subscribe(this)
        displayStatic()
    }

    abstract fun displayTask(tasks: Array<ITask>) : Boolean
    abstract fun displayStatic()
    abstract fun <T:ITTCard> displayTeams(teams : TeamSet<T>) //its own teamIcon shouldn't use the publicTeamIcon, it's for outside viewers

    override fun onTeamClear() {
        // whenever the teams clear, we also want them to clear this
        clear()
    }

    // whenever the team members get an update, we also want the teamIcon to update to represent the correct team members
    override fun onAddMember(member: Player) { teamIcon.refreshTeamVisual() }
    override fun onRemoveMember(member: Player) { teamIcon.refreshTeamVisual() }
    override fun onSwapMember(leave: Player, enter: Player) { teamIcon.refreshTeamVisual() }
}
