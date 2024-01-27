package me.wanttobee.tasktussle.tests


import me.wanttobee.commandtree.ICommandNamespace
import me.wanttobee.commandtree.ICommandObject
import me.wanttobee.commandtree.nodes.CommandEmptyLeaf
import me.wanttobee.commandtree.nodes.ICommandNode
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarSystem
import org.bukkit.Material

object InventoryTestCommands : ICommandNamespace {
    override val commandName: String = "ii"
    override val commandSummary: String = "start something"
    override val hasOnlyOneGroupMember: Boolean = false
    override val isZeroParameterCommand: Boolean = false
    override val systemCommands: Array<ICommandObject> = arrayOf(createBranch, openBranch, debugBranch, clearBranch)

    private var inventory : TestInventory? = null
    object createBranch : ICommandObject{
        override val helpText: String = "creates something"
        override val baseTree: ICommandNode = CommandEmptyLeaf("create") {_ ->
            if(inventory != null) return@CommandEmptyLeaf
            inventory = TestInventory("some cool inventory")
            val item = UniqueItemStack(Material.MAGENTA_BANNER, "hey", "aaa", 3)
            inventory!!.addLockedItem(4,item) { p -> p.sendMessage("hey!!!") }
        }
    }

    object openBranch : ICommandObject{
        override val helpText: String = "open something"
        override val baseTree: ICommandNode = CommandEmptyLeaf("open") {p ->
            inventory?.open(p)
        }
    }

    object clearBranch : ICommandObject{
        override val helpText: String = "clear something"
        override val baseTree: ICommandNode = CommandEmptyLeaf("clear") {_ ->
            inventory?.clear()
            inventory = null
        }
    }
    object debugBranch : ICommandObject{
        override val helpText: String = "debug something"
        override val baseTree: ICommandNode = CommandEmptyLeaf("debug") {p ->
            InteractiveInventorySystem.debugStatus(p)
            InteractiveHotBarSystem.debugStatus(p)
        }
    }
}
