package me.wanttobee.tasktussle

import me.wanttobee.tasktussle.games.bingo.BingoManager
import me.wanttobee.tasktussle.base.games.ITTGameManager
import me.wanttobee.tasktussle.base.tasks.ITaskManager
import me.wanttobee.tasktussle.tasks.achievementTask.AdvancementsFiles
import me.wanttobee.tasktussle.tasks.achievementTask.AdvancementsTaskManager
import me.wanttobee.tasktussle.tasks.clickTask.ClickTaskManager
import me.wanttobee.tasktussle.tasks.obtainTask.ObtainTaskFiles
import me.wanttobee.tasktussle.tasks.obtainTask.ObtainTaskManager

// this object is nothing more than a place where we list all the existing Managers and file managers
object TaskTussleGrouper {
    // when adding a new task, you only have to add it to these 2 lists
    val fileManagers = arrayOf(
        ObtainTaskFiles,
        AdvancementsFiles
    )
    val taskManagers : Array<ITaskManager<*>> = arrayOf(
        ObtainTaskManager,
        AdvancementsTaskManager,
        ClickTaskManager
    )

    // when adding a game you have to add it here
    val gameManagers : Array<ITTGameManager<*>> = arrayOf(
        BingoManager
    )

    fun generateDefaultFolders(){
        for(filesManager in fileManagers)
            filesManager.generateDefaultFolder()
    }
}
