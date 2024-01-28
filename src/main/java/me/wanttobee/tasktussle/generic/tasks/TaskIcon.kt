package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag


class TaskIcon(private val icon: Material, private val taskTitle : String, taskCategory: String, private val progression: ()->String, description : List<String>) {
    // we save the unique items here ,but we don't subscribe to them because we don't really care
    private val item = UniqueItemStack(Material.BARRIER,"unknown",null )
    private var baseLore : MutableList<String>

    fun addToInventory(slot: Int, inv : InteractiveInventory){
        inv.addLockedItem(slot,item)
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

    fun setState(state: TaskState, teamColor: ChatColor? = null, teamTitle : String? = null){
        when (state) {
            TaskState.ACTIVE -> setActive()
            TaskState.FAILED -> setFailed()
            TaskState.COMPLETED -> setCompleted()
            TaskState.LOCKED -> setLocked()
            TaskState.HIDDEN -> setHidden()
            TaskState.COMPLETED_BY -> {
                if (teamColor != null && teamTitle != null)
                    setCompletedBy(teamColor, teamTitle)
                else setFailed()
            }
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
    private fun setCompletedBy(teamColor : ChatColor, teamTitle: String){
        item.type = Material.WHITE_STAINED_GLASS.colorize(teamColor)
        val meta = item.itemMeta!!
        meta.lore = listOf("${teamColor}Completed by $teamTitle")
        meta.setDisplayName("$teamColor$taskTitle")
        item.updateMeta(meta)
        item.updateEnchanted(true)
    }
    private fun setCompleted(){
        item.type = Material.LIME_STAINED_GLASS_PANE
        val meta = item.itemMeta!!
        meta.lore = listOf("${ChatColor.GREEN}Completed")
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
