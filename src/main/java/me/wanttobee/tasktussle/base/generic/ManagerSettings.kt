package me.wanttobee.tasktussle.base.generic

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.util.toLore
import me.wanttobee.tasktussle.base.cards.ITTGameManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

class ManagerSettings(manager: IManager, settingRowCount : Int = 1)  : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null, 9 + 9*settingRowCount, "${manager.subjectName} Settings")
    private val updateItems : MutableList<() -> Unit> = mutableListOf()
    private var settingsIndex = 9

    init{
        val noSettingIcon = UniqueItemStack(
            Material.LIGHT_GRAY_STAINED_GLASS,
            "${ChatColor.GRAY}Empty Setting slot",
            "${ChatColor.DARK_GRAY}a spot for a future setting")
        for(i in 9 until (9 + settingRowCount*9)){
            addLockedItem(i,noSettingIcon)
        }
        for(i in 0 until 9){
            addSeparator(i)
        }

        val isGameManager = manager is ITTGameManager<*>
        val color = if(isGameManager) TaskTussleSettings.gameColor else TaskTussleSettings.taskColor
        val icon = UniqueItemStack(manager.iconMaterial, "$color${manager.subjectName}",
            listOf("${ChatColor.DARK_GRAY}Click: ${ChatColor.GRAY}Go back") +
                    "${ChatColor.GRAY}${manager.iconDescription}".toLore(32)
        ).updateEnchanted(true)
        addLockedItem(4,icon) { player, _ ->
            TaskTussleSettings.open(player)
        }

        TaskTussleSettings.addManagerSettings(manager)
    }


    fun addSetting(item: UniqueItemStack, updateItem: ()->Unit, onClick: (Player, Boolean) -> Unit){
        addSetting(item, updateItem, onClick, onClick)
    }
    fun addSetting(item: UniqueItemStack, updateItem:()->Unit, onLeftClick: (Player, Boolean) -> Unit, onRightClick: (Player, Boolean) -> Unit){
        // note that the itemStack only has to have the attributes that are not changed by the updateItem lambda
        // for example a lore that never changes, or a material that never changes
        updateItem.invoke()
        updateItems.add(updateItem)
        addLockedItem(
            settingsIndex++, item,
            { player, shift ->
                onLeftClick.invoke(player,shift)
                updateItem.invoke()
            }, { player, shift ->
                onRightClick.invoke(player, shift)
                updateItem.invoke()
            })
    }

    override fun openEvent(player: Player, event: InventoryOpenEvent) {
        for(update in updateItems)
            update.invoke()
    }
}