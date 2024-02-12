package me.wanttobee.tasktussle.base.generic

import me.wanttobee.commandtree.ICommandNamespace
import me.wanttobee.commandtree.ICommandObject
import me.wanttobee.commandtree.nodes.*
import me.wanttobee.everythingitems.UniqueItemStack
import me.wanttobee.tasktussle.TaskTussleSystem
import me.wanttobee.tasktussle.TaskTussleSystem.minecraftPlugin
import me.wanttobee.tasktussle.TaskTussleSystem.title
import me.wanttobee.tasktussle.TaskTussleGrouper
import org.bukkit.ChatColor
import org.bukkit.Material
import kotlin.math.max
import kotlin.math.min

object TaskTussleConfig : ICommandNamespace {
    override val commandName: String = "taskTussle"
    override val commandSummary: String = "to start a game or change settings before starting the game"
    override val hasOnlyOneGroupMember: Boolean = false
    override val isZeroParameterCommand: Boolean = false
    override val systemCommands: Array<ICommandObject> = arrayOf(
        StartTree,
        StopTree,
        SettingsTree,
        DebugTree,
        FinishTree
    )

    object StartTree : ICommandObject {
        override val helpText: String = "to start one of the games"
        override val baseTree = CommandBranch("start",
            TaskTussleGrouper.gameManagers.map { manager -> manager.startCommand }.toTypedArray()
        )
    }

    object DebugTree : ICommandObject {
        override val helpText: String = "check some information just about its current running game (no real usage besides that)"
        override val baseTree = CommandBranch("debug", arrayOf(
            CommandEmptyLeaf("status"){ commander ->
                TaskTussleSystem.debugStatus(commander)
            },
            CommandEmptyLeaf("log"){ commander ->
                TaskTussleSystem.canLog = !TaskTussleSystem.canLog
                commander.sendMessage("${title} look at the server console")
                if(TaskTussleSystem.canLog)
                    minecraftPlugin.logger.info("Started Logging")
                else minecraftPlugin.logger.info("Stopped Logging")
            },
        ))
    }

    object StopTree : ICommandObject {
        override val helpText: String = "to stop and clear the current running game"
        override val baseTree = CommandEmptyLeaf("stop") { commander -> TaskTussleSystem.stopGame(commander) }
    }
    object FinishTree : ICommandObject {
        override val helpText: String = "to finish the game early "
        override val baseTree = CommandBranch("finish", arrayOf(
            CommandEmptyLeaf("draw") {commander -> TaskTussleSystem.drawGame(commander) } ,
            CommandEmptyLeaf("out_of_time") {commander -> TaskTussleSystem.outOfTimeGame(commander) } ,
        ))
    }


    object SettingsTree : ICommandObject {
        override val helpText: String = "to view or change settings"
        override val baseTree = CommandEmptyLeaf("settings"){p ->
            TaskTussleSettings.open(p)
            if(TaskTussleSystem.gameIsRunning()) p.sendMessage("${title}${ChatColor.RED} There is already a game running, most settings will not effect a game that is already running")
        }
    }

    init{
        // visibility setting
        val visibilityItem = UniqueItemStack(Material.PAPER, "", null)
        var currentVisiblityIndex = 0
        TaskTussleSettings.addGenericSetting(visibilityItem, {
            visibilityItem
                .updateTitle("${TaskTussleSettings.settingColor}Card Visibility:${ChatColor.YELLOW} ${TaskTussleSystem.cardVisibility}")
                .updateMaterial(when (TaskTussleSystem.cardVisibility){
                    "visible" -> Material.ENDER_EYE
                    "hidden" -> Material.FIREWORK_STAR
                    else -> Material.ENDER_PEARL
                })
                .pushUpdates()
        }) { _, _ ->
            currentVisiblityIndex = (currentVisiblityIndex+1)%TaskTussleSystem.cardVisibilityOptions.size
            TaskTussleSystem.cardVisibility = TaskTussleSystem.cardVisibilityOptions[currentVisiblityIndex]
            TaskTussleSystem.updateVisibility()
        }


        // Easy Ratio
        val easyRatioItem = UniqueItemStack(Material.BRICK, "", TaskTussleSettings.ratioLore)
        TaskTussleSettings.addGenericSetting(easyRatioItem, {
            easyRatioItem
                .updateTitle("${TaskTussleSettings.settingColor}Easy Ratio:${ChatColor.YELLOW} ${TaskTussleSystem.easyRatio}")
                .updateCount(max(1, TaskTussleSystem.easyRatio))
                .updateEnchanted(TaskTussleSystem.easyRatio > 0)
                .pushUpdates()
        }, { _, _ -> TaskTussleSystem.easyRatio = min(100, TaskTussleSystem.easyRatio + 1) },
            { _, shift ->
                if (shift) TaskTussleSystem.easyRatio = 0
                else TaskTussleSystem.easyRatio = max(0, TaskTussleSystem.easyRatio - 1)
            })

        // Normal Ratio
        val normalRatioItem = UniqueItemStack(Material.NETHER_BRICK, "", TaskTussleSettings.ratioLore)
        TaskTussleSettings.addGenericSetting(normalRatioItem, {
            normalRatioItem
                .updateTitle("${TaskTussleSettings.settingColor}Normal Ratio:${ChatColor.YELLOW} ${TaskTussleSystem.normalRatio}")
                .updateCount(max(1, TaskTussleSystem.normalRatio))
                .updateEnchanted(TaskTussleSystem.normalRatio > 0)
                .pushUpdates()
        }, { _, _ -> TaskTussleSystem.normalRatio = min(100, TaskTussleSystem.normalRatio + 1) },
            { _, shift ->
                if (shift) TaskTussleSystem.normalRatio = 0
                else TaskTussleSystem.normalRatio = max(0, TaskTussleSystem.normalRatio - 1)
            })

        // Hard Ratio
        val hardRatioItem = UniqueItemStack(Material.NETHERITE_INGOT, "", TaskTussleSettings.ratioLore )
        TaskTussleSettings.addGenericSetting(hardRatioItem, {
            hardRatioItem
                .updateTitle("${TaskTussleSettings.settingColor}Hard Ratio:${ChatColor.YELLOW} ${TaskTussleSystem.hardRatio}")
                .updateCount(max(1, TaskTussleSystem.hardRatio))
                .updateEnchanted(TaskTussleSystem.hardRatio > 0)
                .pushUpdates()
        }, { _, _ -> TaskTussleSystem.hardRatio = min(100, TaskTussleSystem.hardRatio + 1) },
            { _, shift ->
                if (shift) TaskTussleSystem.hardRatio = 0
                else TaskTussleSystem.hardRatio = max(0, TaskTussleSystem.hardRatio - 1)
            })

        // Chose Teams beforehand
        val choseTeamsItem = UniqueItemStack(Material.PRISMARINE_CRYSTALS, "", null)
        TaskTussleSettings.addGenericSetting(choseTeamsItem, {
            val newTitle = "${TaskTussleSettings.settingColor}Chose Teams: " +
                    if (TaskTussleSystem.choseTeamsBeforehand) "${ChatColor.GREEN}on"
                    else "${ChatColor.RED}off"
            choseTeamsItem
                .updateTitle(newTitle)
                .updateEnchanted(TaskTussleSystem.choseTeamsBeforehand)
                .pushUpdates()
        }) { _, _ -> TaskTussleSystem.choseTeamsBeforehand = !TaskTussleSystem.choseTeamsBeforehand }

        // Game Time
        val clockItem = UniqueItemStack(Material.CLOCK, "", null)
        TaskTussleSettings.addGenericSetting(clockItem, {
            val newTitle = "${TaskTussleSettings.settingColor}Game Time: " +
                    if (TaskTussleSystem.gameTime != 0) "${ChatColor.GREEN}${TaskTussleSystem.gameTime} minutes"
                    else "${ChatColor.RED}disabled"
            clockItem
                .updateTitle(newTitle)
                .updateEnchanted(TaskTussleSystem.gameTime != 0)
                .pushUpdates()
        }) { p, _ -> TimeSettings.open(p) }

        // Load PreSets setting
        val preSetItem = UniqueItemStack(
            Material.WRITABLE_BOOK,
            "${TaskTussleSettings.settingColor}Load a pre-set", null)
        TaskTussleSettings.addLockedItem(8, preSetItem) { p, _ ->
            p.sendMessage("${ChatColor.RED}Does nothing yet")
        }
    }
}
