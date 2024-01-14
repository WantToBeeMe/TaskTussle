package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// there are 2 types of possiblePlayers
// the hardcoded normal list "possiblePlayers" parameter
// and the "realTimePossiblePlayers" which is a lambda which will get the relevant possibilities whenever this command is called
// the last one is needed for when parameters can constantly change, for example whenever you have team names that constantly change
class CommandPlayerLeaf private constructor(argName : String, private val realTimePossiblePlayers : (() -> Collection<Player>)?, private val possiblePlayers: Collection<Player>?, effect : (Player, Player) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Player>(argName,effect, emptyEffect) {
    constructor(argName : String, possiblePlayers: Collection<Player>, effect : (Player, Player) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,null, possiblePlayers,effect,emptyEffect)
    constructor(argName : String, realTimePossiblePlayers : () -> Collection<Player>, effect : (Player, Player) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,realTimePossiblePlayers,null,effect, emptyEffect)

    override val commandParam: String = "(Player)"

    override fun validateValue(sender: Player, tailArgs: Array<String>): Player? {
        if(tailArgs.isEmpty()) return null
        // this is the only place that we give slack for the lowercase() in the arguments
        // these are names and minecraft also handles it so you cant have the same names with different capitalisation
        // so why should we
        if(realTimePossiblePlayers != null){
            for(pos in realTimePossiblePlayers.invoke()){
                if(pos.name.lowercase() == tailArgs.first().lowercase())
                    return pos
            }
        }
        if(possiblePlayers != null){
            for(pos in possiblePlayers){
                if(pos.name.lowercase() == tailArgs.first().lowercase())
                    return pos
            }
        }
        WTBMCommands.sendErrorToSender(sender,tailArgs.first(),"is not online" )
        return null
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if((possiblePlayers != null && possiblePlayers.isEmpty()) || (realTimePossiblePlayers != null && realTimePossiblePlayers.invoke().isEmpty()) ){
            if("" == currentlyTyping)
                list.add("no players available")
        } else {
            if(possiblePlayers != null){
                for (pos in possiblePlayers) {
                    if (pos.name.lowercase().contains(currentlyTyping.lowercase()))
                        list.add(pos.name)
                }
            }
            if(realTimePossiblePlayers != null){
                for (pos in realTimePossiblePlayers.invoke()) {
                    if (pos.name.lowercase().contains(currentlyTyping.lowercase()))
                        list.add(pos.name)
                }
            }
        }
        return list
    }
}