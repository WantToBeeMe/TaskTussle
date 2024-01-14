package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class CommandDoubleLeaf private constructor(argName : String, private val realTimeMin : (() -> Double)?, private val realTimeMax : (() -> Double)?, private val min:Double?, private val max:Double?, effect : (Player, Double) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Double>(argName,effect, emptyEffect) {
    constructor(argName : String, min:Double?, max:Double?, effect : (Player, Double) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,null, null, min, max, effect, emptyEffect)
    constructor(argName : String, min:(() -> Double)?, max:(() -> Double)?, effect : (Player, Double) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,min, max, null, null, effect, emptyEffect)

    override val commandParam: String = "(Number)"

    override fun validateValue(sender: Player, tailArgs: Array<String>): Double? {
        if(tailArgs.isEmpty()) return null
        if(tailArgs.first() == ".."){
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else WTBMCommands.sendErrorToSender(sender,
                "${ChatColor.RED}these ${ChatColor.GRAY}..${ChatColor.RED} are there to convey that you could type any number ${ChatColor.DARK_RED}(Int)${ChatColor.RED}, but not literally ${ChatColor.GRAY}.." )
            return null
        }
        var number = tailArgs.first().toDoubleOrNull() ?: run {
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} is not a valid number.",
                "should be a Double")
            return  null
        }

        val realTimeMinValue = realTimeMin?.invoke()
        val realTimeMaxValue = realTimeMax?.invoke()
        // from here on, the number is correct, but we only need to make sure the number is not too big or small due to the given clamps
        if(min != null && number < min){
            number = min
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $min.",
                "number can only be from $min to $max")
        }
        else if(max != null && number > max){
            number = max
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $max.",
                "number can only be from $min to $max")
        }
        else if(realTimeMinValue != null && number < realTimeMinValue){
            number = realTimeMinValue
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $realTimeMinValue.",
                "number cant be lower than $realTimeMinValue")
        }
        else if(realTimeMaxValue != null && number > realTimeMaxValue){
            number = realTimeMaxValue
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $realTimeMaxValue.",
                "number cant be higher than $realTimeMaxValue")
        }
        return number
    }

    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if (min == null && max == null && realTimeMin == null && realTimeMax == null) {
            if ("" == currentlyTyping)
                list.add("..")
            return list
        }
        val min = min ?: realTimeMin?.invoke()
        val max = max ?: realTimeMax?.invoke()
        // we are not going to put hundreds or even thousands of numbers in the tab complete.
        // so instead we use the .. to indicate that you can put any number in
        // and if its between numbers, it means it's in that range
        if (min == null) {
            if ("" == currentlyTyping) {
                list.add("..")
                list.add((max).toString())
                list.add((max!!- 1.0).toString())
                list.add((max  - 2.0).toString())
            }
        } else if (max == null) {
            if ("" == currentlyTyping) {
                list.add("..")
                list.add((min).toString())
                list.add((min + 1.0).toString())
                list.add((min + 2.0).toString())
            }
        } else {
            if ("" == currentlyTyping) {
                val distance = max - min
                if (distance < 4) {
                    for (i in 0  .. distance.toInt())
                        list.add((min + i.toDouble()).toString())
                } else {
                    list.add((min).toString())
                    list.add((min + 1.0).toString())
                    list.add("..")
                    list.add((max).toString())
                    list.add((max - 1.0).toString())
                }
            }
        }
        return list
    }
}