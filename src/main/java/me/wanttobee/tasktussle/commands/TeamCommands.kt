package me.wanttobee.tasktussle.commands

import me.wanttobee.commandtree.Description
import me.wanttobee.commandtree.ITreeCommand
import me.wanttobee.commandtree.partials.*
import me.wanttobee.tasktussle.teams.TeamSet
import me.wanttobee.tasktussle.teams.TeamSystem

object TeamCommands : ITreeCommand {
    override val description = Description("everything you will need for creating, managing and/or debugging teams")
        .addSubDescription(name= "list", description= "shows the list of active teams")
        .addSubDescription(name= "clearAll", description= "lets you delete all active teams (clear all)")
        .addSubDescription(name= "generateTeams", description= "lets you generate x teams (x is the amount you specified)", usage= "generateTeams <amount>")
        .addSubDescription(name= "teamMaker", description= "will open a team maker with the specific settings your provided", usage= "teamMaker <min team size> <max team size>")

    override val command = BranchPartial("teams").setStaticPartials(
        EmptyPartial("list").setEffect { p -> TeamSystem.debugStatus(p) },
        EmptyPartial("clearAll").setEffect { _ -> TeamSystem.clearAll() },
        IntPartial("generateTeams").setStaticRange(1, null).setEffect { _, i -> TeamSystem.generateTeams(i, "Command Generated") {_,_ -> } },
        IntPartial("teamMaker").setStaticRange(2,54).setEffect { p, i ->
            TeamSystem.startTeamMaker(
                p,
                {_,_ -> },
                i,
                "Command Made"
            ) {_ : TeamSet<*> ->}
        }
    )
}
