package me.wanttobee.tasktussle.base.generic

import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventory
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.base.cards.ITTGameManager
import me.wanttobee.tasktussle.base.tasks.ITaskManager
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory
import kotlin.math.max

// TODO:
//  make it so the settings inventory only exists whenever they are being looked at, but not double
//  so the support that 2 players can access the inventory has to stay there.
//  an other option is to remove the settings whenever you start a game, then the settings are not needed anymore

object TaskTussleSettings : InteractiveInventory() {
    override var inventory: Inventory = Bukkit.createInventory(null, 9*6, "Task Tussle Settings")
    private val updateItems : MutableList<() -> Unit> = mutableListOf()
    val settingColor = ChatColor.GOLD
    val taskColor = ChatColor.AQUA
    val gameColor = ChatColor.LIGHT_PURPLE
    private var settingIndex = 0
    private var taskSettingIndex = 0
    private var cardSettingIndex = 0

    val ratioLore = listOf(
        "${ChatColor.DARK_GRAY}L Click: ${ChatColor.GRAY}Increase ratio",
        "${ChatColor.DARK_GRAY}R Click: ${ChatColor.GRAY}Decrease ratio",
        "${ChatColor.DARK_GRAY}Shift+R Click: ${ChatColor.GRAY}Set ratio to 0,",
        "${ChatColor.GRAY}a ratio of 0 turns it off"
    )
    private val taskLore = listOf(
        "${ChatColor.DARK_GRAY}Shift+L Click: ${ChatColor.GRAY}Open details",
    ) + ratioLore

    private val taskRow = 3*9
    private val gameRow = 5*9
    init{
        fillGapsWithSeparator()

        for(i in 0..8){
            val noTask = UniqueItemStack(Material.LIGHT_GRAY_STAINED_GLASS, "${ChatColor.GRAY}Empty task slot", "${ChatColor.DARK_GRAY}a spot for a future task")
            val noGame = UniqueItemStack(Material.LIGHT_GRAY_STAINED_GLASS, "${ChatColor.GRAY}Empty game slot", "${ChatColor.DARK_GRAY}a spot for a future game")
            addLockedItem(taskRow +i, noTask)
            addLockedItem(gameRow +i, noGame)
        }
    }

    fun addGenericSetting(item: UniqueItemStack, updateItem: ()->Unit, onClick: (Player, Boolean) -> Unit){
        addGenericSetting(item, updateItem, onClick, onClick)
    }
    fun addGenericSetting(item: UniqueItemStack, updateItem:()->Unit, onLeftClick: (Player, Boolean) -> Unit, onRightClick: (Player, Boolean) -> Unit){
        // note that the itemStack only has to have the attributes that are not changed by the updateItem lambda
        // for example a lore that never changes, or a material that never changes
        updateItem.invoke()
        updateItems.add(updateItem)
        addLockedItem(
            settingIndex, item,
            { player, shift ->
                onLeftClick.invoke(player,shift)
                updateItem.invoke()
            }, { player, shift ->
                onRightClick.invoke(player, shift)
                updateItem.invoke()
            })

        // this is really in efficient code, but it works as long as we have max 14 generic settings
        settingIndex++
        if(settingIndex == 7) settingIndex = 9
        if(settingIndex == 15) return
    }

    fun addManagerSettings(managerSettings : IManager){
        if(managerSettings is ITaskManager<*>)
            addTaskSetting(managerSettings)
        if(managerSettings is ITTGameManager<*>)
            addGameSetting(managerSettings)
    }

    private fun addTaskSetting(taskManager: ITaskManager<*>){
        val icon = UniqueItemStack(taskManager.iconMaterial, "", taskLore)
        val updateIcon = {
            val title = "$taskColor${taskManager.taskName}: " +
                    if(taskManager.occupationRatio == 0)
                        "${ChatColor.RED}Disabled"
                    else "${ChatColor.GREEN}Active"
            icon.updateTitle(title)
                .updateCount( max(1,taskManager.occupationRatio))
                .updateEnchanted(taskManager.occupationRatio != 0)
                .pushUpdates()
        }
        updateIcon.invoke()
        updateItems.add(updateIcon)
        addLockedItem(
            taskRow + taskSettingIndex++, icon,
            leftClick@{ player,shift ->
                if(!shift)
                    taskManager.changeOccupationRatioBy(1)
                else taskManager.settingsInventory.open(player)
                updateIcon.invoke()
        }, { _,shift ->
                if(!shift) taskManager.changeOccupationRatioBy(-1)
                else taskManager.setOccupationRatio(0) // essentially setting it inactive
                updateIcon.invoke()
            }
        )
    }

    private fun addGameSetting(gameManager : ITTGameManager<*>){
        val icon = UniqueItemStack(gameManager.iconMaterial, "$gameColor${gameManager.subjectName}",
            "${ChatColor.DARK_GRAY}Click:${ChatColor.GRAY} Open details")
        addLockedItem(
            gameRow + cardSettingIndex++, icon
        ) { p, _ -> gameManager.settingsInventory.open(p) }
    }

    // for now, we update all the icons when someone opens the settings inventory, if they need it or not
    override fun openEvent(player: Player, event: InventoryOpenEvent) {
        for(update in updateItems)
            update.invoke()
    }
}
