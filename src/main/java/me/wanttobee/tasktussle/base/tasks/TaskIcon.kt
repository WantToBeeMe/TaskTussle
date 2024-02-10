package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.Util.toLore
import me.wanttobee.tasktussle.teams.Team
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack


class TaskIcon(private val icon: Material, private val taskTitle : String, taskCategory: String, private val progression: ()->String, description : List<String>) {
    // we save the unique items here ,but we don't subscribe to them because we don't really care
    private val item = UniqueItemStack(Material.BARRIER,"unknown",null )
    private var baseLore : MutableList<String>

    fun addToInventory(slot: Int, inv : InteractiveInventory){
        inv.addLockedItem(slot,item)
    }

    fun isThisItem(other: ItemStack) : Boolean{
        return item.equalsID(other)
    }

    init{
        setHidden()
        item.pushUpdates()
        baseLore = mutableListOf(
            "${ChatColor.GOLD}Task: ${ChatColor.WHITE}$taskCategory",
            "${ChatColor.GREEN}Progress: ${ChatColor.WHITE}${progression.invoke()}",
        )
        val newDesc : MutableList<String> = description.toMutableList()
        for(descLine in newDesc.indices){
            if(descLine == 0)newDesc[descLine] = "${ChatColor.AQUA}Description: ${ChatColor.WHITE}${newDesc[descLine]}"
            else newDesc[descLine] = "${ChatColor.WHITE}${newDesc[descLine]}"
        }
        baseLore += newDesc
    }

    // this method is called whenever you want to update the progression
    fun refreshProgression(){
        baseLore[1] = "${ChatColor.GREEN}Progress: ${ChatColor.WHITE}${progression.invoke()}"
        item.updateLore(baseLore).pushUpdates()
    }

    fun setState(state: TaskState, team: Team? = null, contributors: Array<Player> = emptyArray()){
        when (state) {
            TaskState.ACTIVE -> setActive()
            TaskState.FAILED -> setFailed()
            TaskState.COMPLETED -> setCompleted(contributors)
            TaskState.LOCKED -> setLocked()
            TaskState.HIDDEN -> setHidden()
            TaskState.COMPLETED_BY -> setCompletedBy(team!!,contributors)
        }
        item.pushUpdates()
    }

    private fun setActive(){
        item.type = icon
        val meta = item.itemMeta!!
        meta.lore = baseLore
        meta.setDisplayName("${ChatColor.WHITE}$taskTitle")
        item.updateMeta(meta)
        item.updateEnchanted(false)
    }
    private fun setHidden(){
        item.type = Material.GRAY_STAINED_GLASS_PANE
        val meta = item.itemMeta!!
        meta.lore = listOf("${ChatColor.GRAY}this task is not available (yet)")
        meta.setDisplayName("${ChatColor.GRAY}????")
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        item.updateMeta(meta)
        item.updateEnchanted(false)
    }
    private fun setLocked(){
        item.type = Material.GRAY_STAINED_GLASS_PANE
        val meta = item.itemMeta!!
        meta.lore = listOf("${ChatColor.GRAY}this task is not available (yet)")
        meta.setDisplayName("${ChatColor.GRAY}$taskTitle")
        item.updateMeta(meta)
        item.updateEnchanted(false)
    }
    private fun setCompletedBy( team: Team, contributors: Array<Player>){
        item.type = Material.WHITE_STAINED_GLASS.colorize(team.color)
        val meta = item.itemMeta!!
        val newLore = mutableListOf("${team.color}Completed by ${team.getDisplayName()}")
        newLore += ("${ChatColor.GRAY}" + contributors.joinToString(", ") { p -> p.name }).toLore(35)
        meta.lore = newLore
        meta.setDisplayName("${team.color}$taskTitle")
        item.updateMeta(meta)
        item.updateEnchanted(true)
    }
    private fun setCompleted(contributors: Array<Player>){
        item.type = Material.LIME_STAINED_GLASS_PANE
        val meta = item.itemMeta!!
        val newLore = mutableListOf("${ChatColor.GREEN}Completed")
        newLore += ("${ChatColor.GRAY}" + contributors.joinToString(", ") { p -> p.name }).toLore(35)
        meta.lore =newLore
        meta.setDisplayName("${ChatColor.GREEN}$taskTitle")
        item.updateMeta(meta)
        item.updateEnchanted(true)
    }
    private fun setFailed(){
        item.type = Material.RED_STAINED_GLASS_PANE
        val meta = item.itemMeta!!
        meta.lore = listOf("${ChatColor.RED}Failed")
        meta.setDisplayName("${ChatColor.RED}$taskTitle")
        item.updateMeta(meta)
        item.updateEnchanted(true)
    }
}
