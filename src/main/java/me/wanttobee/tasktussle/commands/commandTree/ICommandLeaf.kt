package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.entity.Player


// We are using a composite design
// that means we have branches and leaves
// branches may have new nodes hanging on it
// however, leaves are normally the end
// in our case however, we modified this design a bit, and we can better explain it as
// branched: a way of sorting the leaves in groups
// leaves: the parameters which are eventually going to be used for the thing you are trying to do
// leaves can have nodes under them, but it becomes messy if you do (just like basically all the big minecraft commands XD)
abstract class ICommandLeaf<T>(argName : String, protected val effect : (Player, T) -> Unit, protected val emptyEffect : ((Player) -> Unit)? = null) : ICommandNode(argName) {

    // some leaves have more arguments, in order to find when this leaf is finished, we need to say how many
    // for example, a position is 3 arguments
    // or varargs has infinite
    // this is needed because in our case we can stack leaves under each other, and we want to know when it ends
    open val argumentsNeeded = 1

    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        // by default, if we don't have any arguments we try to invoke the empty effect
        // otherwise we will return an error to the user
        if(tailArgs.isEmpty()) {
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else  WTBMCommands.sendErrorToSender(sender,"no argument found")
            return
        }
        val value = validateValue(sender, tailArgs) ?: return
        effect.invoke(sender,value)
    }

    // branches are just other grouping up the different leaves
    // but leaves are really values which are eventually used (like booleans or something)
    abstract fun validateValue(sender : Player, tailArgs: Array<String>) : T?
}