package me.wanttobee.tasktussle.taskTussleSystem.tasks.obtainTask

import me.wanttobee.tasktussle.TTPlugin
import me.wanttobee.tasktussle.taskTussleSystem.TaskTussleSystem
import me.wanttobee.tasktussle.taskTussleSystem.tasks.misc.TaskSystem
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader


object ObtainTaskFiles {
    private val plugin = TTPlugin.instance
    private val folder = File(TaskSystem.taskFolder, File.separator + "ObtainTask")
    private const val code_generated_name = "all_obtainable_items"

    fun generateDefaultFolder(): Boolean {
        val defaultFile = "obtainTask_default.yml"
        val inputStream = this.javaClass.getResourceAsStream("/$defaultFile")
        if (inputStream == null) plugin.logger.warning("'$defaultFile' not found in the resources folder.")
        else{
            val fileConfiguration = YamlConfiguration.loadConfiguration(InputStreamReader(inputStream))
            val file = File(folder, File.separator + "default.yml")
            fileConfiguration.save(file)
        }

        val file = File(folder, File.separator + code_generated_name + ".yml")
        val obtainPool: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        if (file.exists()) return !file.exists()
        val hardList = mutableListOf<String>()
        val ultraHardList = mutableListOf<String>()
        plugin.server.scheduler.runTaskAsynchronously(plugin) { _ ->
            obtainPool.createSection("Easy")
            obtainPool.createSection("Normal")
            obtainPool.createSection("Hard")
            obtainPool.setComments("Easy", listOf(
                "all task files are seperated in 3 categories: Easy / Normal / Hard",
                "you can use these to make some tasks more frequent, and some more rare",
                "in game you can use `/taskTussle setting [tt_easyRatio/tt_normalRatio/tt_hardRatio]` to changes these values",
                "by default it is set to:  ${TaskTussleSystem.easyRatio}/${TaskTussleSystem.normalRatio}/${TaskTussleSystem.hardRatio}    meaning, for every ${TaskTussleSystem.easyRatio} Easy, there are ${TaskTussleSystem.normalRatio} Normal, and ${TaskTussleSystem.hardRatio} Hard",
                "(if you dont get that, just thing `the bigger the number the more you get from that category`)",
                " ",
                "in this file you're currently reading everything is just generated by the code with all obtainable items and then just put in the Easy category,",
                "though i did a little bit of effort to split the items you probably and definitely dont want in Normal and Hard (scroll to the end of this file)",
                "I wouldn't use this current file to play, just use the given default or make your own",
                "to change what file you are using to play, /taskTussle settings obtainTask_fileName [otherFile.yml]"
            ))
            val filterMaterials = Material.values().filter itemCheck@ {
                if(!it.isItem) return@itemCheck false
                if(it.name.contains("COMMAND_BLOCK")) return@itemCheck false
                if(it.name.contains("SPAWN_EGG")) return@itemCheck false
                if(it.name.contains("POTION")) return@itemCheck false
                if(it.name.contains("HEAD")) return@itemCheck false
                if(it.name.contains("INFESTED")) return@itemCheck false
                if(it == Material.KNOWLEDGE_BOOK) return@itemCheck false
                if(it == Material.TIPPED_ARROW) return@itemCheck false
                if(it == Material.STRUCTURE_BLOCK) return@itemCheck false
                if(it == Material.STRUCTURE_VOID) return@itemCheck false
                if(it == Material.BARRIER) return@itemCheck false
                if(it == Material.JIGSAW) return@itemCheck false
                if(it == Material.DEBUG_STICK) return@itemCheck false
                if(it == Material.BEDROCK) return@itemCheck false
                if(it == Material.BUNDLE) return@itemCheck false
                if(it == Material.END_PORTAL_FRAME) return@itemCheck false
                if(it == Material.BUDDING_AMETHYST) return@itemCheck false
                if(it == Material.LARGE_FERN) return@itemCheck false
                if(it == Material.PETRIFIED_OAK_SLAB) return@itemCheck false
                if(it == Material.SPAWNER) return@itemCheck false
                if(it == Material.FARMLAND) return@itemCheck false
                if(it == Material.REINFORCED_DEEPSLATE) return@itemCheck false
                if(it == Material.LIGHT) return@itemCheck false
                if(it == Material.DIRT_PATH) return@itemCheck false
                if(it == Material.FROGSPAWN) return@itemCheck false
                if(it == Material.GLOBE_BANNER_PATTERN) return@itemCheck false

                if(
                    it.name.contains("OXIDIZED") || //takes to long
                    it == Material.DRAGON_EGG ||//only for 1 team
                    it == Material.TALL_GRASS) {//only obtainable from a specific village chest (with super low chance)
                    ultraHardList.add(it.name)
                    return@itemCheck false
                }
                if(
                    it.name.contains("WEATHERED") || //takes to long
                    it == Material.CONDUIT ||
                    it == Material.NETHER_STAR ||
                    it == Material.BEACON ||
                    it.name.contains("END_STONE") ||  //we don't just check for "end" because ender-chest is pretty reasonable
                    it.name.contains("SHULKER") ||
                    it.name.contains("PURPUR") ||
                    it.name.contains("CHORUS") ||
                    it == Material.END_ROD ||
                    it == Material.ELYTRA ||
                    it == Material.DRAGON_BREATH ||
                    it == Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE ||
                    it == Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE ||
                    it == Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE ||
                    it == Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE ||
                    it == Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE ||
                    it == Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE ||
                    it == Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE ||
                    it == Material.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE)  {
                    hardList.add(it.name)
                    return@itemCheck false
                }
                true
            }
            val materialList = filterMaterials.map { it.name }
            obtainPool.set("Easy", materialList)
            obtainPool.set("Normal", hardList)
            obtainPool.set("Hard", ultraHardList)

            obtainPool.save(file)
        }
        return true
    }



    fun readFile(name: String): Triple<Array<Material>, Array<Material>, Array<Material>>? {
        val file = getFile(name) ?: return null
        val loadedFile: FileConfiguration = YamlConfiguration.loadConfiguration(file)

        val easyMaterials = loadedFile.getStringList("Easy").mapNotNull { Material.getMaterial(it) }.toTypedArray()

        val normalMaterials = loadedFile.getStringList("Normal").mapNotNull { Material.getMaterial(it) }.toTypedArray()
        val hardMaterials = loadedFile.getStringList("Hard").mapNotNull { Material.getMaterial(it) }.toTypedArray()

        return Triple(easyMaterials, normalMaterials, hardMaterials)
    }


    fun getFile(name: String): File? {
        val fileName = if(name.endsWith(".yml")) name else "$name.yml"
        val file = File(folder, File.separator + fileName)
        return if (file.exists()) file else null
    }

    fun getAllFileNames(): Array<String> {
        if (!folder.exists() || !folder.isDirectory) return emptyArray()
        return folder.list { _, name -> name.endsWith(".yml") && name != "$code_generated_name.yml" } ?: emptyArray()
    }

    fun deleteFile(name: String): Boolean {
        val file = getFile(name) ?: return false
        return file.delete()
    }
}
