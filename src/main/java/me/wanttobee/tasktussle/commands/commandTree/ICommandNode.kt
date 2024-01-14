package me.wanttobee.tasktussle.commands.commandTree

import org.bukkit.entity.Player

//the root of the whole composite command structure (branches and leaves)
abstract class ICommandNode(
    val argName : String
    // The argName is used to reference to this node before this node is being used
    // for example, this arg name could be "say"
    // then this node will do its stuff whenever the argument "say" has been entered before
) {
    abstract val commandParam: String
    //with command: /HelloWorld say 143 5
    // the namespace will be /HelloWorld
    // that means the arguments list 'args' will be ["say", "143", "5"]
    // it will then check all the different CommandObjects if there is one that is assigned  to "say"
    // if so it will enter the next arguments to that object
    //  so there is where this object comes in.
    // lets say this object is the SayObject for the command "say"
    // in that case it will be found and the command will get the tailsArgs: ["143","5"]
    abstract fun onCommand(sender : Player, tailArgs : Array<String>)

    //with command: /HelloWorld say 143 5
    // the namespace will be /HelloWorld and let's say this is the SayObject for command "say"
    // then because the command is already partially filled in, it will see that the first command after the /HelloWorld
    // is "say" and thus will pass all the other arguments to the object tied to this "say" command
    // in other words, this object, and the tailArgs will be ["143", "5"]
    // =-=-=-=
    // then this object wil get all the arguments which is always an array with something
    // it will check  if the length is 1, if it is, it will get on to checking what are all the possible options are
    // if it's larger, it will just like we said above, pass it to the next object that is called "143" and let that object handle it
    fun getTabComplete(sender : Player, tailArgs : Array<String>) : List<String>{
        if(tailArgs.size == 1) return thisTabComplete(sender, tailArgs.first())
        else  if(tailArgs.size > 1) return nextTabComplete(sender,tailArgs.first(), tailArgs.copyOfRange(1, tailArgs.size))
        return emptyList()
    }


    // read the getTabComplete explanation
    // this method is just some code which is part of the getTabComplete method
    // its here, so it's a bit cleaner, but it could be pasted there as well
    // The purpose of this method is to go to the next object that has as command name the "fromArg"
    // it should then return the getTabComplete(tailArgs) from that object
    // (in other words, passing the job of finding the tab complete up to the next object)
    protected open fun nextTabComplete(sender : Player, fromArg: String, tailArgs : Array<String>) : List<String>{
        //the tailArgs are already cut short for the next tabComplete, you don't have to do that anymore
        return emptyList()
    }

    // read the getTabComplete explanation
    // this method is just some code which is part of the getTabComplete method
    // its here, so it's a bit cleaner, but it could be pasted there as well
    // The purpose of this method is to get the tab complete of this object
    //with command: /HelloWorld say 143
    // lets say this is object "say", then the namespace passes it to the object corresponded to the command "say"
    // which is here. this object will see that it is still typing the new argument attached to this-one ("say 143", the "143" bit is next to the "say" bit)
    // that means that it's the job of this object to complete the tab list
    // if the "say" command has to be the last one, then its easy, it will just return an empty list because 143 is not supposed to be there
    // if there is a boolean attacked to it, it will return a list that contains "true" or "false"
    // (though maybe not in this case, the words "true" or "false" both don't start with "143')
    protected abstract fun thisTabComplete(sender : Player, currentlyTyping: String): List<String>
}