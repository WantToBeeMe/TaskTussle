package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// there are 2 types of possibilities
// the hardcoded normal list "possibilities" parameter
// and the "realTimePossibilities" which is a lambda which will get the relevant possibilities whenever this command is called
// the last one is needed for when parameters can constantly change, for example whenever you have team names that constantly change
class CommandStringLeaf private constructor(argName : String, private val realTimePossibilities : (() -> Array<String>)?, private val possibilities : Array<String>?, effect : (Player, String) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<String>(argName,effect, emptyEffect) {
    constructor (argName : String, possibilities : Array<String>?, effect : (Player, String) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName, null, possibilities, effect, emptyEffect)
    constructor (argName : String, realTimePossibilities : () -> Array<String>, effect : (Player, String) -> Unit, emptyEffect : ((Player) -> Unit)? = null) : this(argName, realTimePossibilities,null, effect, emptyEffect)

    override val commandParam: String = "(String)"

    override fun validateValue(sender: Player, tailArgs: Array<String>): String? {
        if(tailArgs.isEmpty()) return null
        // if there are no possibilities, that means every string is possible
        if(possibilities == null && realTimePossibilities == null )
            return tailArgs.first()

        // if there are, that means that the string passed should be in the possibilities provided
        if(possibilities != null){
            for(pos in possibilities){
                if(pos == tailArgs.first())
                    return tailArgs.first()
            }
        }
        if(realTimePossibilities != null){
            for(pos in realTimePossibilities.invoke()){
                if(pos == tailArgs.first())
                    return tailArgs.first()
            }
        }
        WTBMCommands.sendErrorToSender(sender,tailArgs.first(),"is not a valid argument" )
        return null
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if(possibilities == null && realTimePossibilities == null){
            if("" == currentlyTyping)
                list.add("...") // we say ... when you can type whatever you want
        }
        // just like in CommandNamespace we do allow for lowercase in the complete to not annoy people,
        // but it's not allowed when you finally submit the command
        else if(possibilities != null) {
            for (pos in possibilities) {
                if (pos.lowercase().contains(currentlyTyping.lowercase()))
                    list.add(pos)
            }
        } else{
            for (pos in realTimePossibilities!!.invoke()) {
                if (pos.lowercase().contains(currentlyTyping.lowercase()))
                    list.add(pos)
            }
        }
        return list
    }
}