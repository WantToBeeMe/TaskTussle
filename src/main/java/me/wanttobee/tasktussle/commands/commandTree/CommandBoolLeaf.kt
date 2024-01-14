package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandBoolLeaf(argName : String, effect : (Player, Boolean) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Boolean>(argName,effect, emptyEffect) {

    override val commandParam: String = "(true/false)"

    override fun validateValue(sender: Player, tailArgs: Array<String>): Boolean? {
        if(tailArgs.isEmpty()) return null
        val bool = tailArgs.first().toBooleanStrictOrNull() ?: run {
            WTBMCommands.sendErrorToSender(sender,"${tailArgs.first()} is not a valid boolean", "(true/false)" )
            return null
        }
        return bool
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if("true".startsWith(currentlyTyping.lowercase())) list.add("true")
        if("false".startsWith(currentlyTyping.lowercase())) list.add("false")
        return list
    }
}