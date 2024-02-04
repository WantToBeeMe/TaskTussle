package me.wanttobee.tasktussle.tasks.achievementTask

import me.wanttobee.tasktussle.generic.tasks.ITaskFiles
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.advancement.AdvancementDisplayType
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration


object AdvancementsFiles : ITaskFiles("AdvancementsTask","all_advancements.yml", "advancementTask_default.yml") {

    // we always return a triple, because the base game is divided up in to 3 groups, Easy Normal and Hard
    fun readFile(name: String): Triple<Array<NamespacedKey>, Array<NamespacedKey>, Array<NamespacedKey>>? {
        val file = getFile(name) ?: return null
        val loadedFile: FileConfiguration = YamlConfiguration.loadConfiguration(file)

        val easyAdvancements = loadedFile.getStringList(EASY).mapNotNull { NamespacedKey.fromString(it) }.toTypedArray()
        val normalAdvancements = loadedFile.getStringList(NORMAL).mapNotNull {NamespacedKey.fromString(it) }.toTypedArray()
        val hardAdvancements = loadedFile.getStringList(HARD).mapNotNull { NamespacedKey.fromString(it) }.toTypedArray()

        return Triple(easyAdvancements, normalAdvancements, hardAdvancements)
    }

    override fun generateDefaultFolder(): Boolean {
        val base = super.generateDefaultFolder()
        if (!base) return false

        createFile(allPossibilitiesFileName).configureAndSaveYaml(true) {
            it.createSection(EASY)
            it.createSection(NORMAL)
            it.createSection(HARD)
            it.setComments(EASY, ratioExplanation + listOf(
                "Here in this file, all the advancements are generated.",
                "I should not use this file to play, this file is purely to give you all the possible advancements",
                "To play with advancements you can either use default.yml or create your own file by assembling one with these given advancements",
                "I did take the effort to split the different types in the Easy,Normal and Hard, in this file they are split as followed:",
                "Easy = TASK        (advancements with squire icon frame)",
                "Normal = CHALLENGE (advancements with stylised icon frame)",
                "Hard = GOAL        (advancements with round icon frame)"
            ))
            val easyList = mutableListOf<String>()
            val normalList = mutableListOf<String>()
            val hardList = mutableListOf<String>()

            for(advancement in Bukkit.advancementIterator()){
                val display = advancement.display ?: continue
                val key = advancement.key
                when (display.type) {
                    AdvancementDisplayType.TASK -> easyList.add(key.toString())
                    AdvancementDisplayType.CHALLENGE -> normalList.add(key.toString())
                    /*AdvancementDisplayType.GOAL*/ else -> hardList.add(key.toString())
                }
            }
            it.set(EASY, easyList)
            it.set(NORMAL, normalList)
            it.set(HARD, hardList)
        }
        return true
    }
}