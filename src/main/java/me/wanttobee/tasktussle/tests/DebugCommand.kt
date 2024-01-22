package me.wanttobee.tasktussle.tests

import me.wanttobee.commandTree.ICommandNamespace
import me.wanttobee.commandTree.ICommandObject
import me.wanttobee.commandTree.commandTree.CommandEmptyLeaf
import me.wanttobee.commandTree.commandTree.ICommandNode
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarSystem
import me.wanttobee.tasktussle.teams.TeamSystem

object DebugCommand : ICommandNamespace {
    override val commandName: String = "debug_tt"
    override val commandSummary: String = "check the different statuses of the different aspects of this plugin"
    override val hasOnlyOneGroupMember: Boolean = false
    override val isZeroParameterCommand: Boolean = false
    override val systemCommands: Array<ICommandObject> = arrayOf(HotBarItems,IInventory,Teams)

    object HotBarItems : ICommandObject{
        override val helpText: String = "will print a list of all the active hotbar items"
        override val baseTree: ICommandNode = CommandEmptyLeaf("hotbar_items") { p ->
            InteractiveHotBarSystem.debugStatus(p)
        }
    }
    object IInventory : ICommandObject{
        override val helpText: String = "will print a list of all the active interactive inventories"
        override val baseTree: ICommandNode = CommandEmptyLeaf("interactive_inventories") { p ->
            InteractiveInventorySystem.debugStatus(p)
        }
    }
    object Teams : ICommandObject{
        override val helpText: String = "will print a list of all the active teams"
        override val baseTree: ICommandNode = CommandEmptyLeaf("teams") { p ->
            TeamSystem.debugStatus(p)
        }
    }
}
