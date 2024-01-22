package me.wanttobee.tasktussle.generic.cards

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material

class TeamIcon(private val ownerInventory : InteractiveInventory, private val associatedTeam : Team, private val totalTaskAmount : Int?){
    private val publicTeamIcon = UniqueItemStack(
        Material.WHITE_STAINED_GLASS.colorize(associatedTeam.color), associatedTeam.getDisplayName(), null)
    private val privateTeamIcon = UniqueItemStack(
        Material.WHITE_CONCRETE.colorize(associatedTeam.color), "${associatedTeam.color}This Card", null, true)

    // updateInventory is the list that will be invoked whenever the teamIcon has to be updated (for example changing the input)
    private val updateInventory: MutableList<()->Unit> = mutableListOf()
    private val disableClickEvent : MutableList<()->Unit> = mutableListOf()
    private val enableClickEvent : MutableList<()->Unit> = mutableListOf()
    private var clickable = true

    init{
        updateTeam()
        setAmount(0)
    }

    // add to any interactiveInventory
    fun addToInventory(slot: Int, inv : InteractiveInventory){
        // if the provided inventory is teams inventory, then it will do the privateTeamIcon
        if(inv == ownerInventory){
            inv.addLockedItem(slot,privateTeamIcon )
            updateInventory.add {
                inv.updateItem(privateTeamIcon)
            }
        }
        // otherwise it is not the current inventory, and then it will do the public team icon
        else {
            // we add it to the inventory
            if(clickable)
                inv.addLockedItem(slot,publicTeamIcon){ player -> ownerInventory.open(player) }
            else
                inv.addLockedItem(slot,publicTeamIcon)

            // we make sure that the inventory we just added this item to, also gets added to the update callbacks
            updateInventory.add {
                inv.updateItem(publicTeamIcon)
            }

            // we make sure that we can disable and enable the item
            disableClickEvent.add {
                inv.removeItem(publicTeamIcon)
                inv.addLockedItem(slot,publicTeamIcon,null)
            }
            enableClickEvent.add {
                inv.removeItem(publicTeamIcon)
                inv.addLockedItem(slot,publicTeamIcon) { player ->
                    ownerInventory.open(player)
            } }
        }
    }

    // if you change the itemStack, call the updateIcon method to make sure that changes
    // are applied in the inventory
    // (changes to the event does not count, disabling and enabling also not)
    private fun updateIcon(){
        for(thing in updateInventory)
            thing.invoke()
    }


    fun setAmount(amount : Int){
        val amountText = "${ChatColor.GRAY}$amount/$totalTaskAmount"
        publicTeamIcon.amount = if(amount < 1) 1 else amount
        privateTeamIcon.amount = if(amount < 1) 1 else amount

        // changing the public amount
        val publicMeta = publicTeamIcon.itemMeta!!
        val publicLore = publicMeta.lore!!
        publicLore[publicLore.size - 1] = amountText
        publicMeta.lore = publicLore
        publicTeamIcon.setItemMeta(publicMeta)

        // changing the private amount to the same count
        val privateMeta = privateTeamIcon.itemMeta!!
        val privateLore = privateMeta.lore!!
        privateLore[privateLore.size - 1] = amountText
        privateMeta.lore = privateLore
        privateTeamIcon.setItemMeta(privateMeta)

        //afterwords we call update to make sure all inventories apply the new changes
        updateIcon()
    }

    fun setClickable(value : Boolean) {
        if(value) publicTeamIcon.type = Material.WHITE_CONCRETE.colorize(associatedTeam.color)
        else publicTeamIcon.type =  Material.WHITE_STAINED_GLASS.colorize(associatedTeam.color)
        updateIcon() // we made changes to the itemStack
        if(clickable != value){
            if(value) for (enableEvent in enableClickEvent) { enableEvent.invoke() }
            else for (disableEvent in disableClickEvent) { disableEvent.invoke() }
        }
        clickable = value
    }

    // this sets for example the team member list under the item
    fun updateTeam(){
        val memberString = "${ChatColor.GRAY}${associatedTeam.getMemberString()}"
        val amountText = "${ChatColor.GRAY}0/$totalTaskAmount"
        val publicMeta = publicTeamIcon.itemMeta!!
        publicMeta.lore = listOf(memberString,amountText )
        publicTeamIcon.itemMeta = publicMeta

        val privateMeta = privateTeamIcon.itemMeta!!
        privateMeta.lore = listOf(memberString,amountText)
        privateTeamIcon.itemMeta = privateMeta
        updateIcon()
    }
}
