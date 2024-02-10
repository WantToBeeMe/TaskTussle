package me.wanttobee.tasktussle.base.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader

// This is the base structure that task files need to follow
abstract class ITaskFiles(taskName: String, protected val allPossibilitiesFileName : String, protected val defaultResourceFileName: String?) {
    val EASY = "Easy"
    val NORMAL = "Normal"
    val HARD = "Hard"
    protected val ratioExplanation = listOf("all tasks (and thus their files) are seperated in 3 categories: Easy / Normal / Hard",
        "you can use these to make some tasks more frequent, and some more rare",
        "in game you can use: `/taskTussle settings`, this will open a settings menu and at the top you can change these values",
        "by default it is set to:  ${TaskTussleSystem.easyRatio}/${TaskTussleSystem.normalRatio}/${TaskTussleSystem.hardRatio}   meaning, for every ${TaskTussleSystem.easyRatio} Easy, there are ${TaskTussleSystem.normalRatio} Normal, and ${TaskTussleSystem.hardRatio} Hard",
        "(if you still dont understand,then just think `the bigger the number the more you get from that category`)",
        " ")

    private val taskFolder = File(TaskTussleSystem.TaskTussleBaseFolder, File.separator + taskName)
    protected fun createFile(fileName: String) : File{
        return File(taskFolder, File.separator + fileName)
    }
    fun File.configureAndSaveYaml(runAsync: Boolean, configuration: (FileConfiguration) -> Unit){
        val fileConfiguration: FileConfiguration = YamlConfiguration.loadConfiguration(this)
        if(runAsync){
            TaskTussleSystem.minecraftPlugin.server.scheduler
                .runTaskAsynchronously(TaskTussleSystem.minecraftPlugin) { _ ->
                    configuration.invoke(fileConfiguration)
                    fileConfiguration.save(this)
                }
        }
        else{
            configuration.invoke(fileConfiguration)
            fileConfiguration.save(this)
        }
    }

    fun getFile(name: String): File? {
        val fileName = if(name.endsWith(".yml")) name else "$name.yml"
        val file = File(taskFolder, File.separator + fileName)
        return if (file.exists()) file else null
    }

    fun getAllFileNames(): Array<String> {
        if (!taskFolder.exists() || !taskFolder.isDirectory) return emptyArray()
        return taskFolder.list { _, name -> name.endsWith(".yml") && name != allPossibilitiesFileName } ?: emptyArray()
    }

    fun deleteFile(name: String): Boolean {
        val file = getFile(name) ?: return false
        return file.delete()
    }

    // when overriding this method, make sure to put `super.generateDefaultFolder()` in that override
    // otherwise you won't get the default file anymore which is bad
    open fun generateDefaultFolder(): Boolean {
        if(defaultResourceFileName == null)
            return true // if there is no default file, this can all be skipped

        val inputStream = this.javaClass.getResourceAsStream("/$defaultResourceFileName")
        if (inputStream == null) {
            TaskTussleSystem.minecraftPlugin.logger.warning("'$defaultResourceFileName' not found in the resources folder.")
            return false
        }
        else {
            val fileConfiguration = YamlConfiguration.loadConfiguration(InputStreamReader(inputStream))
            val file = createFile("default.yml")
            fileConfiguration.save(file)
        }
        return true
    }


}
