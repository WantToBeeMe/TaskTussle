package me.wanttobee.tasktussle.generic.cards


import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.Util.toLore
import me.wanttobee.tasktussle.generic.TaskTussleSettings
import me.wanttobee.tasktussle.generic.TaskTussleSettings.gameColor
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory

class TTGameSettings<T:ITTCard>(private val manager: ITTGameManager<T>) : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null, 9, "${manager.gameName} Settings")
    private val updateItems : MutableList<() -> Unit> = mutableListOf()
    private var settingsIndex = 2 // we start the settings from index 2
    init{
        setTaskIcon()
        addSeparator(1)
        TaskTussleSettings.addGameSetting(manager)
    }

    private fun setTaskIcon(){
        val gameIcon = UniqueItemStack(manager.gameIconMaterial, "${gameColor}${manager.gameName}",
            listOf("${ChatColor.DARK_GRAY}Click: ${ChatColor.GRAY}Go back") +
                    "${ChatColor.GRAY}${manager.gameDescription}".toLore(32)
            ).updateEnchanted(true)
        addLockedItem(0,gameIcon) { player, _ ->
            TaskTussleSettings.open(player)
        }
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
