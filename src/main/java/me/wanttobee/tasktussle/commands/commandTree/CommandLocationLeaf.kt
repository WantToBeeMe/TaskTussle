package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player

class CommandLocationLeaf(argName : String, effect : (Player, Location) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Location>(argName,effect, emptyEffect) {

    override val commandParam: String = "(x:number) (y:number) (z:number)"
    override val argumentsNeeded: Int = 3

    override fun validateValue(sender: Player, tailArgs: Array<String>): Location? {
        if(tailArgs.size < 3) return null
        val x = toLocation(sender, "x", tailArgs[0]) ?: run {
            WTBMCommands.sendErrorToSender(sender,"${tailArgs[0]} is not a valid number","(x)" )
            return null
        }
        val y = toLocation(sender, "y", tailArgs[1]) ?: run {
            WTBMCommands.sendErrorToSender(sender,"${tailArgs[1]} is not a valid number","(y)" )
            return null
        }
        val z = toLocation(sender, "z", tailArgs[2]) ?: run {
            WTBMCommands.sendErrorToSender(sender,"${tailArgs[2]} is not a valid number","(z)" )
            return null
        }
        return Location(sender.world,x,y,z)
    }
    private fun toLocation(sender: Player, direction: String, numberCord: String) : Double?{
        if(numberCord ==  "~") return getSenderLocation(sender, direction)

        var numberToTranslate = numberCord
        if(numberCord.startsWith('~'))
            numberToTranslate = numberCord.drop(1)
        var cord = numberToTranslate.toDoubleOrNull() ?: return null
        if(numberCord.startsWith('~'))
            cord += getSenderLocation(sender, direction)
        return cord
    }
    private fun getSenderLocation(sender : Player, direction: String) : Double{
        return when (direction) {
            "x" -> sender.location.x
            "y" -> sender.location.y
            "z" -> sender.location.z
            else -> -1.0
        }
    }


    override fun nextTabComplete(sender: Player, fromArg: String, tailArgs: Array<String>): List<String> {
        val targetBlock = sender.getTargetBlock(null, 6)
        val blockLocation = if(targetBlock.type.isAir) null else targetBlock.location

        if(tailArgs.size == 1){
            if(tailArgs[0] != ""){
                if(toLocation(sender, "y", tailArgs[0]) == null) return emptyList()
                return listOf(
                   "${tailArgs[0]} ${blockLocation?.blockZ ?: "~"}",
                )
            }
            return listOf(
                "${blockLocation?.blockY ?: "~"}",
                "${blockLocation?.blockY ?: "~"} ${blockLocation?.blockZ ?: "~"}",
            )
        }
        if(tailArgs.size == 2 && tailArgs[1] == "")
            return listOf("${blockLocation?.blockZ ?: "~"}")
        return emptyList()
    }
    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val targetBlock = sender.getTargetBlock(null, 6)
        val blockLocation = if(targetBlock.type.isAir) null else targetBlock.location

        if(currentlyTyping != ""){
            if(toLocation(sender, "x", currentlyTyping) == null) return emptyList()
            return listOf(
                "$currentlyTyping ${blockLocation?.blockY ?: "~"}",
                "$currentlyTyping ${blockLocation?.blockY ?: "~"} ${blockLocation?.blockZ ?: "~"}",
            )
        }
        return listOf(
            "${blockLocation?.blockX ?: "~"}",
            "${blockLocation?.blockX ?: "~"} ${blockLocation?.blockY ?: "~"}",
            "${blockLocation?.blockX ?: "~"} ${blockLocation?.blockY ?: "~"} ${blockLocation?.blockZ ?: "~"}",
        )
    }
}