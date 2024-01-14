package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.entity.Player

// The pair is if you want multiple parameters under one arg name
// just like how vararg is infinite parameters of the same type, this is 2 parameters of 2 types of your choice,
// so it can be /HelloWorld say WantToNeeMe true
// where say is this and command object, and the pair is <Player, Boolean>
class CommandPairLeaf<T,U>(argName : String, private val firstLeaf : ICommandLeaf<T>, private val secondLeaf : ICommandLeaf<U>, effect : (Player, Pair<T,U>) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Pair<T,U>>(argName,effect, emptyEffect) where T : Any, U : Any {
    constructor(argName: String, firstLeaf: ICommandLeaf<T>, secondLeaf: ICommandLeaf<U>) : this(argName,firstLeaf,secondLeaf,{ _, _ -> })

    override val commandParam: String = "${firstLeaf.commandParam} ${secondLeaf.commandParam}"
    override val argumentsNeeded: Int = firstLeaf.argumentsNeeded + secondLeaf.argumentsNeeded

    override fun onCommand(sender: Player, tailArgs: Array<String>) {
        if(tailArgs.isEmpty()) {
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else WTBMCommands.sendErrorToSender(sender, "not enough arguments found")
            return
        }
        val pair = validateValue(sender, tailArgs) ?: run{
            WTBMCommands.sendErrorToSender(sender, "not enough arguments found")
            return
        }
        effect.invoke(sender,pair)
    }

    override fun validateValue(sender: Player, tailArgs: Array<String>): Pair<T,U>? {
        if(tailArgs.isEmpty()) return null
        val firstValue : T = firstLeaf.validateValue(sender, tailArgs) ?: return null
        val secondValue : U = secondLeaf.validateValue(sender, tailArgs.copyOfRange(firstLeaf.argumentsNeeded, tailArgs.size)) ?: return null
        return Pair(firstValue,secondValue)
    }

    // let's say we have a Pair<Pair<A,B>, Pair<C,D> >
    // if the arguments are ["pair","A","B","C"]
    // then nextTabComplete will be called with  Pair.nextTabComplete(player, "A", ["B","C"])
    // in other words, Pair is the object, and his tab complete job is "A", however as you can se we are already done with that,
    // so now it's the next parameters job to do the tab complete. in particular, it's the job of "A"  to do it
    // That's why it is given as the fromArg
    override fun nextTabComplete(sender: Player, fromArg: String, tailArgs: Array<String>): List<String> {
        if(tailArgs.size < firstLeaf.argumentsNeeded){
            // T tab complete
            // as long as the tailArgs list is smaller, we know it belongs to firstLeaf.
            // that's because fromArg is taken from the list, and thus if size is 2, it will always belong to tabComplete if the list is 0 or 1
            return firstLeaf.getTabComplete(sender,arrayOf(fromArg) + tailArgs)
        } else{
            // U tab complete
            // however, when the list becomes the same size (so in other words, list+fromArg becomes bigger)
            // then it is overflowing the first Leaf, and  thus going to the second Leaf
            return secondLeaf.getTabComplete(sender,tailArgs.copyOfRange(firstLeaf.argumentsNeeded-1, tailArgs.size))
        }

    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        //T tabComplete
        // we don't have to check, the first parameter always belongs to the first leaf
        return firstLeaf.getTabComplete(sender, arrayOf(currentlyTyping))
    }
}