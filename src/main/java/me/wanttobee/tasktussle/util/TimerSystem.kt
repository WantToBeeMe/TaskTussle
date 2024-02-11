package me.wanttobee.tasktussle.util

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

object TimerSystem {
    private val timers = mutableMapOf<String, Timer>()

    fun createTimer(timerName : String, plugin: Plugin, seconds: Int, bossBar: Boolean, actionOnTimeUp: () -> Unit): Boolean {
        if (timers.containsKey(timerName)) return false
        val timer = Timer(plugin, seconds, bossBar, actionOnTimeUp)
        timers[timerName] = timer
        return true
    }

    fun clearTimer(name : String){
        val timer = timers.remove(name) ?: return
        timer.clear()
    }
    fun pauseTimer(name: String){
        val timer = timers[name] ?: return
        timer.pause()
    }
    fun resumeTimer(name: String){
        val timer = timers[name] ?: return
        timer.resume()
    }

    fun debugStatus(commander : Player){
        commander.sendMessage("[TimerSystem] ${ChatColor.YELLOW}active timers:")
        if(timers.isEmpty()){
            commander.sendMessage("${ChatColor.GREEN}there are no timers active")
            return
        }
        for(timer in timers)
            commander.sendMessage("${ChatColor.YELLOW}- ${ChatColor.RESET}${timer.key}")
    }
}
