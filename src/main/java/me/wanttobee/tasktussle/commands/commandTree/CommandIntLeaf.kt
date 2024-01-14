package me.wanttobee.tasktussle.commands.commandTree

import me.wanttobee.tasktussle.commands.WTBMCommands
import org.bukkit.ChatColor
import org.bukkit.entity.Player

// there are 2 types of possibilities
// the hardcoded normal list "possibilities" parameter
// and the "realTimePossibilities" which is a lambda which will get the relevant possibilities whenever this command is called
// the last one is needed for when parameters can constantly change, for example whenever you have team names that constantly change
class CommandIntLeaf private constructor(argName : String, private val realTimeMin : (() -> Int)?, private val realTimeMax : (() -> Int)?, private val min:Int?, private val max:Int?, private val realTimePossibilities : (() -> Collection<Int>)?, private val possibilities : Collection<Int>?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : ICommandLeaf<Int>(argName,effect, emptyEffect) {
    constructor(argName : String, min:Int?, max:Int?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,null,null,min, max,null,null, effect, emptyEffect )
    constructor(argName : String, possibilities: Collection<Int>?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,null,null,null, null,null,possibilities, effect, emptyEffect )
    constructor(argName : String, min: (() -> Int)?, max: (() -> Int)?, effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,min,max,null, null,null,null, effect, emptyEffect )
    constructor(argName : String, possibilities:  (() -> Collection<Int>), effect : (Player, Int) -> Unit, emptyEffect : ((Player) -> Unit)? = null ) : this(argName,null,null,null, null,possibilities,null, effect, emptyEffect )

    override val commandParam: String = "(Number)"

    override fun validateValue(sender: Player, tailArgs: Array<String>): Int? {
        if(tailArgs.isEmpty()) return null
        if(tailArgs.first() == ".."){
            if(emptyEffect != null) emptyEffect.invoke(sender)
            else WTBMCommands.sendErrorToSender(sender,
                "${ChatColor.RED}these ${ChatColor.GRAY}..${ChatColor.RED} are there to convey that you could type any number ${ChatColor.DARK_RED}(Int)${ChatColor.RED}, but not literally ${ChatColor.GRAY}.." )
            return null
        }
        var number = tailArgs.first().toIntOrNull() ?: run {
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} is not a valid number.",
                "should be an Integer (Int)")
            return null
        }

        if(possibilities != null && !possibilities.contains(number)){
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} is not a valid number.",
                "you must chose from one of the suggested once")
            return null
        }
        if(realTimePossibilities != null && !realTimePossibilities.invoke().contains(number)){
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} is not a valid number.",
                "you must chose from one of the suggested once")
            return null
        }

        val realTimeMinValue = realTimeMin?.invoke()
        val realTimeMaxValue = realTimeMax?.invoke()
        // from here on, the number is correct, but we only need to make sure the number is not too big or small due to the given clamps
        if(min != null && number < min && (possibilities == null && realTimePossibilities == null)){
            number = min
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $min.",
                "number can only be from $min to $max")
        }
        else if(max != null && number > max  && (possibilities == null && realTimePossibilities == null)){
            number = max
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $max.",
                "number can only be from $min to $max")
        }
        else if(realTimeMinValue != null && number < realTimeMinValue  && (possibilities == null && realTimePossibilities == null)){
            number = realTimeMinValue
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $realTimeMinValue.",
                "number cant be lower than $realTimeMinValue")
        }
        else if(realTimeMaxValue != null && number > realTimeMaxValue  && (possibilities == null && realTimePossibilities == null)){
            number = realTimeMaxValue
            WTBMCommands.sendErrorToSender(sender,
                "${tailArgs.first()} has been clamped to $realTimeMaxValue.",
                "number cant be higher than $realTimeMaxValue")
        }

        return number
    }


    override fun thisTabComplete(sender: Player, currentlyTyping: String): List<String> {
        val list = mutableListOf<String>()
        if(possibilities != null){
            for(p in possibilities)
                if (p.toString().startsWith(currentlyTyping)) list.add(p.toString())
            return list
        }
        if(realTimePossibilities != null){
            for(p in realTimePossibilities.invoke())
                if (p.toString().startsWith(currentlyTyping)) list.add(p.toString())
            return list
        }
        if (min == null && max == null && realTimeMin == null && realTimeMax == null) {
            if ("" == currentlyTyping){
                list.add("..")
            }
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
                list.add((max!!- 1).toString())
                list.add((max  - 2).toString())
            }
        } else if (max == null) {
            if ("" == currentlyTyping) {
                list.add("..")
                list.add((min).toString())
                list.add((min + 1).toString())
                list.add((min + 2).toString())
            }
        } else {
            if ("" == currentlyTyping) {
                if (max - min < 6) {
                    for (i in min  .. max)
                        list.add(i.toString())
                } else {
                    list.add("$min..$max")
                }
            }
        }
        return list
    }
}