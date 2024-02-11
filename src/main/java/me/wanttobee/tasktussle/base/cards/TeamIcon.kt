package me.wanttobee.tasktussle.base.cards

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.util.toLore
import me.wanttobee.tasktussle.base.tasks.ITask
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material

class TeamIcon(private val ownerInventory : InteractiveInventory, private val associatedTeam : Team, private val totalTaskAmount : Int?){
    // we save the unique items here ,but we don't subscribe to them because we don't really care
    private val publicTeamIcon = UniqueItemStack(
        Material.WHITE_STAINED_GLASS.colorize(associatedTeam.color), associatedTeam.getDisplayName(), null)
    private val privateTeamIcon = UniqueItemStack(
        Material.WHITE_CONCRETE.colorize(associatedTeam.color), "${associatedTeam.color}This Card", null, true)

    private val disableClickEvent : MutableList<()->Unit> = mutableListOf()
    private val enableClickEvent : MutableList<()->Unit> = mutableListOf()
    private var clickable = true
    private var completedCount = 0

    init{
        refreshTeamVisual()
        setAmount(0)
    }

    // add to any interactiveInventory
    fun addToInventory(slot: Int, inv : InteractiveInventory){
        // if the provided inventory is teams inventory, then it will do the privateTeamIcon
        if(inv == ownerInventory)
            inv.addLockedItem(slot,privateTeamIcon )
        // otherwise it is not the current inventory, and then it will do the public team icon
        else {
            // we add it to the inventory
            if(clickable)
                inv.addLockedItem(slot,publicTeamIcon){ player,_ -> ownerInventory.open(player) }
            else
                inv.addLockedItem(slot,publicTeamIcon)

            // we make sure that we can disable and enable the item
            // TODO:
            //  make the disabling and enabling more efficient, currently it will replace the item with itself, with or without the event
            //  we could make it so it will only replace the event
            disableClickEvent.add {
                inv.removeItem(publicTeamIcon)
                inv.addLockedItem(slot,publicTeamIcon,null)
            }
            enableClickEvent.add {
                inv.removeItem(publicTeamIcon)
                inv.addLockedItem(slot,publicTeamIcon) { player,_ ->
                    ownerInventory.open(player)
            } }
        }
    }

    fun setAmount(amount : Int){
        completedCount = amount
        val amountText = "${ChatColor.GRAY}$completedCount/$totalTaskAmount"
        publicTeamIcon.updateCount(if(completedCount < 1) 1 else completedCount)
        privateTeamIcon.updateCount(if(completedCount < 1) 1 else completedCount)

        // changing the public amount
        val publicMeta = publicTeamIcon.itemMeta!!
        val publicLore = publicMeta.lore!!
        publicLore[publicLore.size - 1] = amountText
        publicMeta.lore = publicLore
        publicTeamIcon.updateMeta(publicMeta).pushUpdates()

        // changing the private amount to the same count
        val privateMeta = privateTeamIcon.itemMeta!!
        val privateLore = privateMeta.lore!!
        privateLore[privateLore.size - 1] = amountText
        privateMeta.lore = privateLore
        privateTeamIcon.updateMeta(privateMeta).pushUpdates()
    }

    fun setClickable(isClickable : Boolean) {
        if(isClickable) publicTeamIcon.updateMaterial(Material.WHITE_CONCRETE.colorize(associatedTeam.color)).pushUpdates()
        else publicTeamIcon.updateMaterial(Material.WHITE_STAINED_GLASS.colorize(associatedTeam.color)).pushUpdates()
        if(clickable != isClickable){
            if(isClickable) for (enableEvent in enableClickEvent) { enableEvent.invoke() }
            else for (disableEvent in disableClickEvent) { disableEvent.invoke() }
        }
        clickable = isClickable
    }

    // this recreates the teams visual under the item, can be useful whenever a team changes,
    // that you can make it refresh the list that it shows
    fun refreshTeamVisual(){
        val newLore : MutableList<String> = mutableListOf()
        val amountText = "${ChatColor.GRAY}$completedCount/$totalTaskAmount"
        val memberString = "${ChatColor.GRAY}" + associatedTeam.getMembers().joinToString(", ") { p -> p.name }
        newLore += memberString.toLore(35) + amountText
        publicTeamIcon.updateLore(newLore).pushUpdates()
        privateTeamIcon.updateLore(newLore).pushUpdates()
    }

    fun showContributions(taskList: Array<ITask>){
        val newLore : MutableList<String> = mutableListOf()
        newLore += "${ChatColor.GRAY}$completedCount/$totalTaskAmount"
        val allCompleted = taskList.filter { task -> task.stateCode.isCompleted && task.completerTeam == associatedTeam }
        val totalCompleted = allCompleted.count()
        for(member in associatedTeam.getMembers()){
            val thisParticipation = allCompleted.count { task -> task.contributors.contains(member.name) }
            val percentage = ((thisParticipation/totalCompleted.toFloat()) * 10000).toInt().toFloat() / 100
            newLore += "${ChatColor.GRAY}${member.name} = $percentage%"
        }
        publicTeamIcon.updateLore(newLore).pushUpdates()
        privateTeamIcon.updateLore(newLore).pushUpdates()
    }
}
