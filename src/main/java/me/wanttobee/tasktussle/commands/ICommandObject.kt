package me.wanttobee.tasktussle.commands

import me.wanttobee.tasktussle.commands.commandTree.ICommandNode

//an interface to build your command tree from
interface ICommandObject {

    val helpText : String

    val baseTree : ICommandNode
}



