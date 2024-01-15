package me.wanttobee.tasktussle.teams

import me.wanttobee.commandTree.ICommandNamespace
import me.wanttobee.commandTree.ICommandObject
import me.wanttobee.commandTree.commandTree.CommandEmptyLeaf
import me.wanttobee.commandTree.commandTree.CommandIntLeaf
import me.wanttobee.commandTree.commandTree.ICommandNode


// you already have researched : TeamCommands
// you already have researched : TeamMakerInventory
// you already have researched : TeamSystem
// TODO:
//  research the file : Team
//  research the file : TeamSet
//  research the file : ITeamSetObserver
//  research the file : ITeamObserver

object TeamCommands : ICommandNamespace {
    override val commandName: String = "teams"
    override val commandSummary: String = "everything you will need for creating, managing and/or debugging teams"
    override val hasOnlyOneGroupMember: Boolean = false
    override val isZeroParameterCommand: Boolean = false
    override val systemCommands: Array<ICommandObject> = arrayOf(ListTree,ClearTree, GenerateTree, MakeTree );

    object ListTree : ICommandObject{
        override val helpText: String = "shows the list of active teams"
        override val baseTree: ICommandNode = CommandEmptyLeaf("list") {p -> TeamSystem.listTeams(p) }
    }
    object ClearTree : ICommandObject{
        override val helpText: String = "lets you delete all active teams (clear all)"
        override val baseTree: ICommandNode = CommandEmptyLeaf("clearAll") { _ -> TeamSystem.clearAll() }
    }
    object GenerateTree : ICommandObject{
        override val helpText: String = "lets you generate x teams (x is the amount you specified)"
        override val baseTree: ICommandNode = CommandIntLeaf("generateTeams", null,  {_,i -> TeamSystem.makeTeams(i,"Command Generated") {} } )
    }
    object MakeTree : ICommandObject{
        override val helpText: String = "will open a team maker with the specific settings your provided"
        override val baseTree: ICommandNode =CommandIntLeaf("teamMaker", 2, 54,  {p,i -> TeamSystem.teamMaker(p,{},i,"Command Made"){} } )
    }
}