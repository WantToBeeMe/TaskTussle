package me.wanttobee.tasktussle.commands

import me.wanttobee.commandtree.Description
import me.wanttobee.commandtree.ITreeCommand
import me.wanttobee.commandtree.partials.BranchPartial
import me.wanttobee.commandtree.partials.EmptyPartial
import me.wanttobee.commandtree.partials.ICommandPartial
import me.wanttobee.tasktussle.util.WorldSetupHelper.startLobby
import me.wanttobee.tasktussle.util.WorldSetupHelper.clearLobby

object WorldSetupCommands : ITreeCommand {
    override val description = Description("used to set the border and saturation effect and stuff like that")
        .addSubDescription("start", "to start the lobby thing")
        .addSubDescription("clear", "to clear the lobby thing")

    override val command: ICommandPartial = BranchPartial("worldSetup").setStaticPartials(
        EmptyPartial("start").setEffect { invoker -> startLobby(invoker) },
        EmptyPartial("clear").setEffect { invoker -> clearLobby(invoker) },
    )
}

