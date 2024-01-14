package me.wanttobee.tasktussle

import me.wanttobee.tasktussle.commands.ICommandNamespace
import me.wanttobee.tasktussle.commands.ICommandObject
import me.wanttobee.tasktussle.commands.commandTree.*
import org.bukkit.ChatColor

object HelloWorldCommands : ICommandNamespace {
    override val commandName: String = "helloWorld"
    override val commandSummary: String = "to say hello world or something"
    override val systemCommands: Array<ICommandObject> = arrayOf(GroupTree,coolPairTree)
    override val hasOnlyOneGroupMember: Boolean = false
    override val isZeroParameterCommand: Boolean = false


    object BoolTree : ICommandObject{
        override val helpText: String = "boolean yes"
        override val baseTree = CommandBoolLeaf("bool",
            {commander, bool -> commander.sendMessage("${ChatColor.LIGHT_PURPLE}$bool") } )
    }

    object SayTree : ICommandObject {
        override val helpText: String = "say it"
        override val baseTree = CommandEmptyLeaf("say") { commander -> commander.sendMessage("${ChatColor.YELLOW}Hello World") }
    }

    object PairTree : ICommandObject {
        override val helpText: String = "This is my cool pair tree"
        override val baseTree: ICommandNode = CommandPairLeaf("pair",
            CommandBoolLeaf("bool", {_,_ -> } ),
            CommandBoolLeaf("bool2", {_,_ -> } ),
            {sender, pair -> sender.sendMessage("${ChatColor.RED}${pair.first}${ChatColor.LIGHT_PURPLE}${pair.second}")}
            )
    }

    object GroupTree : ICommandObject {
        override val helpText: String = "to group all the messy stuff we just made"
        override val baseTree: ICommandNode = CommandBranch("group", arrayOf(
            BoolTree.baseTree,
            SayTree.baseTree,
            PairTree.baseTree
            ))
    }


    object coolPairTree : ICommandObject {
        override val helpText: String = "this is such an cool pair"
        override val baseTree: ICommandNode = CommandTripleLeaf(
            "myCoolPair",
            CommandLocationLeaf("coolInt", {_,_->}),
            CommandPairLeaf( "firstPair",
                CommandBoolLeaf("firstBool", {_,_ ->}),
                CommandBoolLeaf("secondBool", {_,_ ->})
            )
            , CommandPairLeaf(
                "secondPair",
                CommandIntLeaf("coolInt", null, {_,_->}),
                CommandStringLeaf("strinCool", null, {_,_->})
            )
            , {sender,pair -> sender.sendMessage("(${pair.first}) - (${pair.second.first},${pair.second.second}) - (${pair.third.first},${pair.third.second})")}
        )
    }


}