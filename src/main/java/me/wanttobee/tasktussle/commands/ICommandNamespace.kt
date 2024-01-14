package me.wanttobee.tasktussle.commands

import org.bukkit.ChatColor
import org.bukkit.entity.Player

//the base structure of the composite design
//the flowerPot of the branches and leaves if you will
//this is the first words you will type in a given command
// in other words, /HelloWorld do something 23
// then this  first bit '/HelloWorld' is the nameSpace or at least I am calling it that way now
// all the other ICommandObjects will live under that namespace or under any of the other part of that space
// so 'do' will live directly in '/HelloWorld' and 'something' will live in 'do'
// and '23' will be a number object living under 'something'

interface ICommandNamespace : IPlayerCommands {
    val commandName : String
    val commandSummary : String
    val systemCommands : Array<ICommandObject>

    // this is for when you have only 1 argument name in your command
    // for example /CustomTellRaw say "hello this is something I want to say"
    // if 'say' is the only commandObject here, there is no need for everyone to constantly type "say" if it's the only possible thing they can type
    // in that case you can set this to true
    val hasOnlyOneGroupMember : Boolean

    //with command: /HelloWorld say 143 5
    // the namespace will be /HelloWorld
    // that means the arguments list 'args' will be ["say", "143", "5"]
    // it will then check all the different CommandObjects if there is one that is assigned  to "say"
    // if so it will enter the next arguments to that object
    //  so in this case, SayObject.onCommand(player, ["143","5"])
    override fun onCommand(sender: Player, args: Array<String>): Boolean {
        var ranCommand = false
        if(hasOnlyOneGroupMember){
            systemCommands.first().baseTree.onCommand(sender,args)
            return true
        }
        for(sysCom in systemCommands){
            if (sysCom.baseTree.argName == args.first()) {
                sysCom.baseTree.onCommand(sender, args.copyOfRange(1, args.size))
                ranCommand = true
                break
            }
        }
        if(!ranCommand){
            WTBMCommands.sendErrorToSender(sender,"that is not a valid command.",args.joinToString(" "))
        }

        return true
    }

    override fun help(sender: Player, page : Int){
        val amountPerPage = 8
        val totalPages = (systemCommands.size/amountPerPage)+1
        val page = Math.min(page,totalPages)
        val helperTab : (String)-> String = { h -> "${ChatColor.YELLOW}$h${ChatColor.WHITE}"}
        sender.sendMessage("${ChatColor.GRAY}-========= ${ChatColor.WHITE}$page/$totalPages ${ChatColor.GRAY}=========-")
        if(page == 1) {
            if(WTBMCommands.title != null) sender.sendMessage(
                "${WTBMCommands.title} ${ChatColor.YELLOW}/$commandName${ChatColor.WHITE} $commandSummary"
            )
            else sender.sendMessage("[${ChatColor.YELLOW}$commandName${ChatColor.RESET}] $commandSummary")
        }

        // when there is only 1 group member we don't have to display each object as a different command, because there is only 1 possibility
        if(hasOnlyOneGroupMember){
            if(!isZeroParameterCommand)  // if there are parameters, there is no need of showing this command again
                sender.sendMessage("${ChatColor.GRAY}/$commandName ${systemCommands.first().baseTree.commandParam}")
            sender.sendMessage("${ChatColor.GRAY}-========= ${ChatColor.WHITE}$page/$totalPages ${ChatColor.GRAY}=========-")
            return
        }

        for(sysCom in 0 until amountPerPage){
            val index = sysCom + (page-1)*amountPerPage
            if(systemCommands.size <= index) break
            val command = systemCommands[sysCom]
            val commandExample = "${ChatColor.GRAY}/$commandName ${command.baseTree.argName} ${command.baseTree.commandParam}"
            sender.sendMessage("- ${helperTab(command.baseTree.argName + ":")} ${command.helpText} $commandExample")
        }
        sender.sendMessage("${ChatColor.GRAY}-========= ${ChatColor.WHITE}$page/$totalPages ${ChatColor.GRAY}=========-")
    }

    override fun onTabComplete(sender: Player, args: Array<String>): List<String> {
        val list : MutableList<String> = mutableListOf();

        if(hasOnlyOneGroupMember)
            return systemCommands.first().baseTree.getTabComplete(sender, args)

        // we don't want to annoy people with accidentally having caps on, so the tab complete
        // will still work when you write the capitalisation wrong
        if(args.size == 1){
            for(sysCom in systemCommands){
                if(sysCom.baseTree.argName.lowercase().startsWith(args.first().lowercase()))
                    list.add(sysCom.baseTree.argName)
            }
            return list
        }

        // however, if you finished the word, and it is capitalised wrong, then it will not work any further
        // that's because the command itself won't work due to wrong capitalisation
        // So we don't want to bring over the wrong idea
        for(sysCom in systemCommands){
            // we find the command object that is the first argument, and let that
            // object handle the tab complete from there
            if(sysCom.baseTree.argName == args.first())
                return sysCom.baseTree.getTabComplete(sender, args.copyOfRange(1, args.size) )
        }
        return list
    }

}