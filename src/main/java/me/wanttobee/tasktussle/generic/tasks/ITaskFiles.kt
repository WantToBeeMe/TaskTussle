package me.wanttobee.tasktussle.generic.tasks

import me.wanttobee.tasktussle.TaskTussleSystem
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader

// This is the base structure that task files need to follow
abstract class ITaskFiles(taskName: String, protected val allPossibilitiesFileName : String, protected val defaultResourceFileName: String) {
    protected val taskFolder = File(TaskTussleSystem.TaskTussleBaseFolder, File.separator + taskName)

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

    // we always return a triple, because the base game is divided up in to 3 groups, Easy Normal and Hard
    fun readFile(name: String): Triple<Array<Material>, Array<Material>, Array<Material>>? {
        val file = getFile(name) ?: return null
        val loadedFile: FileConfiguration = YamlConfiguration.loadConfiguration(file)

        val easyMaterials = loadedFile.getStringList("Easy").mapNotNull { Material.getMaterial(it) }.toTypedArray()

        val normalMaterials = loadedFile.getStringList("Normal").mapNotNull { Material.getMaterial(it) }.toTypedArray()
        val hardMaterials = loadedFile.getStringList("Hard").mapNotNull { Material.getMaterial(it) }.toTypedArray()

        return Triple(easyMaterials, normalMaterials, hardMaterials)
    }

    // when overriding this method, make sure to put `super.generateDefaultFolder()` in that override
    // otherwise you won't get the default file anymore which is bad
    open fun generateDefaultFolder(): Boolean {
        val inputStream = this.javaClass.getResourceAsStream("/$defaultResourceFileName")
        if (inputStream == null) {
            TaskTussleSystem.minecraftPlugin.logger.warning("'$defaultResourceFileName' not found in the resources folder.")
            return false
        }
        else {
            val fileConfiguration = YamlConfiguration.loadConfiguration(InputStreamReader(inputStream))
            val file = File(taskFolder, File.separator + "default.yml")
            fileConfiguration.save(file)
        }
        return true
    }
}
