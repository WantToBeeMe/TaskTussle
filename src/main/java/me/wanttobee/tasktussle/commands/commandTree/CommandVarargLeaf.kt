package me.wanttobee.tasktussle.commands.commandTree

import org.bukkit.entity.Player

// because we want to specify a type to the vararg, we have to validate eventually
// this happens in the validate method, but we also need to know what to validate
// so for that we also provide a CommandLeaf that is the single parameter variant of this vararg
// (soo boolean, string, or int)
class CommandVarargLeaf<T>(argName : String, private val typeReferenceLeaf : ICommandLeaf<T>, private val canReturnEmpty : Boolean, effect : (Player, List<T>) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<List<T>>(argName, effect, emptyEffect) {

    // some leaves have more arguments, in order to find when this leaf is finished, we need to say how many
    // for example, a position is 3 arguments
    // this is needed because in our case we can stack leaves under each other, and we want to know when it ends
    // in this case a vararg would be infinite, however, that's quite hard to create,
    // so we set it at some large number
    // there is  not reason for the number 1000 other than that it's big, you will never be able to reach that
    // with the minecraft chat limit anyway, and if you where, who would type 1000 arguments O_O
    override val argumentsNeeded = 1000
    override val commandParam: String = "[${typeReferenceLeaf.commandParam} ,${typeReferenceLeaf.commandParam} ,..."

    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        if(canReturnEmpty && tailArgs.isEmpty())
            effect.invoke(sender, emptyList())
        else super.onCommand(sender, tailArgs)
    }
    override fun validateValue(sender : Player, tailArgs: Array<String>) : List<T>? {
        val listT : MutableList<T> =  mutableListOf()
        for(arg in tailArgs){
            val potentialT = typeReferenceLeaf.validateValue(sender, arrayOf(arg)) ?: return null
            listT.add(potentialT)
        }
        return listT
    }

    override fun nextTabComplete(sender: Player, fromArg: String, tailArgs: Array<String>): List<String> {
        // we make sure that we only pass in an array of size 1, that way the tab complete gets handled
        // by the leaf we use as a type reference
        return typeReferenceLeaf.getTabComplete(sender, arrayOf(tailArgs.last()))
    }
    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        // we make sure that we only pass in an array of size 1, that way the tab complete gets handled
        // by the leaf we use as a type reference
        return typeReferenceLeaf.getTabComplete(sender, arrayOf(currentlyTyping))
    }
}