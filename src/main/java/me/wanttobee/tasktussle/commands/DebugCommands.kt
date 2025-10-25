package me.wanttobee.tasktussle.commands

import me.wanttobee.commandtree.Description
import me.wanttobee.commandtree.ITreeCommand
import me.wanttobee.commandtree.partials.BranchPartial
import me.wanttobee.commandtree.partials.EmptyPartial
import me.wanttobee.commandtree.partials.ICommandPartial
import me.wanttobee.everythingitems.interactiveinventory.InteractiveInventorySystem
import me.wanttobee.everythingitems.interactiveitems.InteractiveHotBarSystem
import me.wanttobee.tasktussle.base.tasks.TaskEventsListener
import me.wanttobee.tasktussle.teams.TeamSystem
import me.wanttobee.tasktussle.util.TimerSystem

object DebugCommands : ITreeCommand  {
    override val description = Description("check the different statuses of the different aspects of this plugin")
        .addSubDescription("task_events", "will print the amount of tasks that are active, and how much events they have")
        .addSubDescription("hotbar_items", "will print a list of all the active hotbar items")
        .addSubDescription("interactive_inventories", "will print a list of all the active interactive inventories")
        .addSubDescription("teams", "will print a list of all the active teams")
        .addSubDescription("timers", "will print a list of all the active timers")

    override val command: ICommandPartial = BranchPartial("debug_tt").setStaticPartials(
        EmptyPartial("task_events").setEffect { p -> TaskEventsListener.debugStatus(p) },
        EmptyPartial("hotbar_items").setEffect { p -> InteractiveHotBarSystem.debugStatus(p) },
        EmptyPartial("interactive_inventories").setEffect { p -> InteractiveInventorySystem.debugStatus(p) },
        EmptyPartial("teams").setEffect { p ->  TeamSystem.debugStatus(p) },
        EmptyPartial("timers").setEffect { p -> TimerSystem.debugStatus(p) }
    )
}
