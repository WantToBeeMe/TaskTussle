package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class FileNamePicker(private val fileManager: ITaskFiles,private val taskManager: ITaskManager<*>,private val pageIndex : Int = 0) : InteractiveInventory() {
    private val files :Array<String> = fileManager.getAllFileNames()
    private val neededRows = (files.size-1)/9 + 1
    override var inventory: Inventory = Bukkit.createInventory(null,
        9+(minOf(5*9,9 * neededRows ) ),
        "File Name picker" + if(neededRows*9 > 5*9) " - Page:$pageIndex" else "")

    init{
        TaskTussleSystem.log("created file selector menu")
        fillGapsWithSeparator()

        val endIndex = minOf(files.size, (pageIndex+1) * 5*9)
        val startIndex = pageIndex * 5*9
        for(fileIndex in startIndex until endIndex){
            val fileName = files[fileIndex]

            val icon = UniqueItemStack(if(fileIndex%2 == 0) Material.PAPER else Material.MAP,
                "${ChatColor.YELLOW}$fileName",null)
            if(taskManager.fileName == fileName)
                icon.updateEnchanted(true)
                    .updateLore(listOf("${ChatColor.GRAY}current"))

            addLockedItem(fileIndex - startIndex, icon){p,_ ->
                taskManager.fileName = fileName
                taskManager.settingsInventory.open(p)
            }
        }
        navBar()
    }
    private fun navBar(){
        if(neededRows*9 <= 5*9)
            return
        // 5 rows per page (+1 because it starts counting from 0)
        val totalPages =((neededRows-1)/5) + 1
        val bowIcon = UniqueItemStack(Material.BOW, "${ChatColor.GRAY}${pageIndex+1}/$totalPages",null)
        addLockedItem(5*9 + 4, bowIcon)
        if(pageIndex > 0){
            val arrowIcon = UniqueItemStack(Material.ARROW, "${ChatColor.WHITE}Go Back",null)
            addLockedItem(5*9, arrowIcon) {p,_ ->
                FileNamePicker(fileManager,taskManager,pageIndex-1 ).open(p)
            }
        }
        if(pageIndex+1 < totalPages){
            val arrowIcon = UniqueItemStack(Material.ARROW, "${ChatColor.WHITE}Go Next",null)
            addLockedItem(6*9 -1, arrowIcon) {p,_ ->
                FileNamePicker(fileManager,taskManager,pageIndex+1 ).open(p)
            }
        }
    }

    // we only want this inventory to exist whenever we open it
    override fun closeEvent(player: Player, event: InventoryCloseEvent) {
        // THIS IS THE CLEAR EVENT, WITHOUT THE BIT THAT CLOSES ALL THE VIEWER
        separator.unsubscribe(this)
        InteractiveInventorySystem.removeInventory(this)
        TaskTussleSystem.log("removed file selector menu")
    }
}