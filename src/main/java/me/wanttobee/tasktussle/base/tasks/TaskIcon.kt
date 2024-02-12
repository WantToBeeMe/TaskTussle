package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.util.toLore
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack


class TaskIcon(val iconMaterial: Material, val taskTitle : String, taskCategory: String, private val progression: ()->String, description : String) {
    // we save the unique items here ,but we don't subscribe to them because we don't really care
    private val uniqueItem = UniqueItemStack(Material.BARRIER,"unknown",null )
    private var baseLore : MutableList<String>

    fun addToInventory(slot: Int, inv : InteractiveInventory){
        inv.addLockedItem(slot,uniqueItem)
    }

    fun isThisItem(other: ItemStack) : Boolean{
        return uniqueItem.equalsID(other)
    }

    init{
        setHidden()
        uniqueItem.pushUpdates()
        baseLore = mutableListOf(
            "${ChatColor.GOLD}Task: ${ChatColor.WHITE}$taskCategory",
            "${ChatColor.GREEN}Progress: ${ChatColor.WHITE}${progression.invoke()}",
        )
        baseLore += "${ChatColor.AQUA}Description: ${ChatColor.WHITE}$description".toLore(35)
        // // this line below makes it more cluttered than it already is, so i rather not, but maybe i must...
        // baseLore += "${ChatColor.DARK_GRAY}Right Click: ${ChatColor.GRAY}options & details"
    }

    // this method is called whenever you want to update the progression
    fun refreshProgression(){
        baseLore[1] = "${ChatColor.GREEN}Progress: ${ChatColor.WHITE}${progression.invoke()}"
        uniqueItem.updateLore(baseLore).pushUpdates()
    }

    fun setState(state: TaskState, team: Team? = null, contributors: Set<String> = emptySet()){
        when (state) {
            TaskState.ACTIVE -> setActive()
            TaskState.FAILED -> setFailed()
            TaskState.COMPLETED -> setCompleted(contributors)
            TaskState.LOCKED -> setLocked()
            TaskState.HIDDEN -> setHidden()
            TaskState.COMPLETED_BY -> setCompletedBy(team!!,contributors)
        }
        uniqueItem.pushUpdates()
    }

    private fun setActive(){
        uniqueItem.type = iconMaterial
        val meta = uniqueItem.itemMeta!!
        meta.lore = baseLore
        meta.setDisplayName("${ChatColor.WHITE}$taskTitle")
        uniqueItem.updateMeta(meta)
        uniqueItem.updateEnchanted(false)
    }
    private fun setHidden(){
        uniqueItem.type = Material.GRAY_STAINED_GLASS_PANE
        val meta = uniqueItem.itemMeta!!
        meta.lore = listOf("${ChatColor.GRAY}this task is not available (yet)")
        meta.setDisplayName("${ChatColor.GRAY}????")
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        uniqueItem.updateMeta(meta)
        uniqueItem.updateEnchanted(false)
    }
    private fun setLocked(){
        uniqueItem.type = Material.GRAY_STAINED_GLASS_PANE
        val meta = uniqueItem.itemMeta!!
        meta.lore = listOf("${ChatColor.GRAY}this task is not available (yet)")
        meta.setDisplayName("${ChatColor.GRAY}$taskTitle")
        uniqueItem.updateMeta(meta)
        uniqueItem.updateEnchanted(false)
    }
    private fun setCompletedBy( team: Team, contributors: Set<String>){
        uniqueItem.type = Material.WHITE_STAINED_GLASS.colorize(team.color)
        val meta = uniqueItem.itemMeta!!
        val newLore = mutableListOf("${team.color}Completed by ${team.getDisplayName()}")
        newLore += ("${ChatColor.GRAY}" + contributors.joinToString(", ")).toLore(35)
        meta.lore = newLore
        meta.setDisplayName("${team.color}$taskTitle")
        uniqueItem.updateMeta(meta)
        uniqueItem.updateEnchanted(true)
    }
    private fun setCompleted(contributors: Set<String>){
        uniqueItem.type = Material.LIME_STAINED_GLASS_PANE
        val meta = uniqueItem.itemMeta!!
        val newLore = mutableListOf("${ChatColor.GREEN}Completed")
        newLore += ("${ChatColor.GRAY}" + contributors.joinToString(", ")).toLore(35)
        meta.lore =newLore
        meta.setDisplayName("${ChatColor.GREEN}$taskTitle")
        uniqueItem.updateMeta(meta)
        uniqueItem.updateEnchanted(true)
    }
    private fun setFailed(){
        uniqueItem.type = Material.RED_STAINED_GLASS_PANE
        val meta = uniqueItem.itemMeta!!
        meta.lore = listOf("${ChatColor.RED}Failed")
        meta.setDisplayName("${ChatColor.RED}$taskTitle")
        uniqueItem.updateMeta(meta)
        uniqueItem.updateEnchanted(true)
    }
}
