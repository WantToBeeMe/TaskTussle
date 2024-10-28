package me.wanttobee.tasktussle.teams
/*
import me.wanttobee.commandtree.ICommandNamespace
import me.wanttobee.commandtree.ICommandObject
import me.wanttobee.commandtree.nodes.CommandEmptyLeaf
import me.wanttobee.commandtree.nodes.CommandIntLeaf
import me.wanttobee.commandtree.nodes.ICommandNode
*/

object TeamCommands  { //ICommandNamespace
    /*
    override val commandName: String = "teams"
    override val commandSummary: String = "everything you will need for creating, managing and/or debugging teams"
    override val hasOnlyOneGroupMember: Boolean = false
    override val isZeroParameterCommand: Boolean = false
    override val systemCommands: Array<ICommandObject> = arrayOf(ListTree,ClearTree, GenerateTree, MakeTree );

    object ListTree : ICommandObject{
        override val helpText: String = "shows the list of active teams"
        override val baseTree: ICommandNode = CommandEmptyLeaf("debug") {p -> TeamSystem.debugStatus(p) }
    }
    object ClearTree : ICommandObject{
        override val helpText: String = "lets you delete all active teams (clear all)"
        override val baseTree: ICommandNode = CommandEmptyLeaf("clearAll") { _ -> TeamSystem.clearAll() }
    }
    object GenerateTree : ICommandObject{
        override val helpText: String = "lets you generate x teams (x is the amount you specified)"
        override val baseTree: ICommandNode = CommandIntLeaf("generateTeams", null,  {_,i -> TeamSystem.generateTeams(i,"Command Generated") {} } )
    }
    object MakeTree : ICommandObject{
        override val helpText: String = "will open a team maker with the specific settings your provided"
        override val baseTree: ICommandNode =CommandIntLeaf("teamMaker", 2, 54,  {p,i -> TeamSystem.startTeamMaker(p,{},i,"Command Made"){} } )
    }
     */
}
