package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.everythingitems.ItemUtil.colorize
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag


class TaskIcon(private val icon: Material, private val taskTitle : String, taskCategory: String, private val progression: ()->String, description : List<String>) {
    private val item = UniqueItemStack(Material.BARRIER,"null",null )
    private var lore : MutableList<String>

    private val updateInventory: MutableList<()->Unit> = mutableListOf()

    fun addToInventory(slot: Int, inv : InteractiveInventory){
        inv.addLockedItem(slot,item)
        updateInventory.add {
            inv.updateItem(item)
        }
    }

    init{
        setHidden()
        lore = mutableListOf(
            "${ChatColor.GOLD}Task: ${ChatColor.WHITE}$taskCategory",
            "${ChatColor.GREEN}Progress: ${ChatColor.WHITE}${progression.invoke()}",
        )
        val newDesc : MutableList<String> = description.toMutableList()
        for(descLine in newDesc.indices){
            if(descLine == 0)newDesc[descLine] = "${ChatColor.AQUA}Description: ${ChatColor.WHITE}${newDesc[descLine]}"
            else newDesc[descLine] = "${ChatColor.WHITE}${newDesc[descLine]}"
        }
        lore += newDesc

    }

    private fun update(){
        for(up in updateInventory)
            up.invoke()
    }

    // this method is called whenever you want to update the progression
    fun updateProgression(){
        lore[1] = "${ChatColor.GREEN}Progress: ${ChatColor.WHITE}${progression.invoke()}"
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.lore = lore
        item.setItemMeta(meta)
        update()
    }

    private fun enchant(yes : Boolean){
        if (yes) { item.addUnsafeEnchantment(Enchantment.DURABILITY, 1) }
        else item.removeEnchantment(Enchantment.DURABILITY)
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskTussle/TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        item.setItemMeta(meta)
        update()
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
        update()
    }

    // these methods all are invoked by setState() which has at the end the update() method
    // that means you don't have to call update() at the end of the following methods
    private fun setActive(){
        item.type = icon
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.lore = lore
        meta.setDisplayName("${ChatColor.WHITE}$taskTitle")
        item.setItemMeta(meta)
        enchant(false)
    }
    private fun setHidden(){
        item.type = Material.GRAY_STAINED_GLASS_PANE
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.lore = listOf("${ChatColor.GRAY}this task is not available (yet)")
        meta.setDisplayName("${ChatColor.GRAY}????")
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
        item.setItemMeta(meta)
        enchant(false)
    }
    private fun setLocked(){
        item.type = Material.GRAY_STAINED_GLASS_PANE
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.lore = listOf("${ChatColor.GRAY}this task is not available (yet)")
        meta.setDisplayName("${ChatColor.GRAY}$taskTitle")
        item.setItemMeta(meta)
        enchant(false)
    }
    private fun setCompletedBy(teamColor : ChatColor, teamTitle: String){
        item.type = Material.WHITE_STAINED_GLASS.colorize(teamColor)
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.lore = listOf("${teamColor}Completed by $teamTitle")
        meta.setDisplayName("$teamColor$taskTitle")
        item.setItemMeta(meta)
        enchant(true)
    }
    private fun setCompleted(){
        item.type = Material.LIME_STAINED_GLASS_PANE
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.lore = listOf("${ChatColor.GREEN}Completed")
        meta.setDisplayName("${ChatColor.GREEN}$taskTitle")
        item.setItemMeta(meta)
        enchant(true)
    }
    private fun setFailed(){
        item.type = Material.RED_STAINED_GLASS_PANE
        val meta = item.itemMeta ?: run{
            TaskTussleSystem.minecraftPlugin.logger.info("(TaskIcon) ERROR: cant access itemMeta for taskIcon: $taskTitle")
            return
        }
        meta.lore = listOf("${ChatColor.RED}Failed")
        meta.setDisplayName("${ChatColor.RED}$taskTitle")
        item.setItemMeta(meta)
        enchant(true)
    }
}
