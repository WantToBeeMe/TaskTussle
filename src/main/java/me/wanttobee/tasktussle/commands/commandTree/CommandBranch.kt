package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// We are using a composite design
// that means we have branches and leaves
// branches may have new nodes hanging on it
// however, leaves are normally the end
// in our case however, we modified this design a bit, and we can better explain it as
// branched: a way of sorting the leaves in groups
// leaves: the parameters which are eventually going to be used for the thing you are trying to do
// leaves can have nodes under them, but it becomes messy if you do (just like basically all the big minecraft commands XD)

class CommandBranch(argName: String, private val branches : Array<ICommandNode> ) : ICommandNode(argName) {

    override val commandParam: String = "..."

    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        if(tailArgs.isEmpty()){
            WTBMCommands.sendErrorToSender(sender,"not enough arguments found")
            return
        }
        for(branch in branches) {
            if (branch.argName.lowercase() == tailArgs.first().lowercase()){
                branch.onCommand(sender, tailArgs.copyOfRange(1, tailArgs.size) )
                return
            }
        }
        WTBMCommands.sendErrorToSender(sender,"${tailArgs.first()} is not a valid argument")
    }

    override fun nextTabComplete(sender: Player, fromArg:String, tailArgs: Array<String>): List<String> {
        for(branch in branches){
            if(branch.argName.lowercase() == fromArg.lowercase())
                return branch.getTabComplete(sender, tailArgs)
        }
        return emptyList()
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String) : List<String> {
        val list = mutableListOf<String>()
        for(branch in branches)
            if(branch.argName.lowercase().contains(currentlyTyping.lowercase())) list.add(branch.argName)
        return list
    }
}