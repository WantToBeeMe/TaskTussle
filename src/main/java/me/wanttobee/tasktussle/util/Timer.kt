package me.wanttobee.tasktussle.util

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

class Timer(
    private val plugin: Plugin, seconds: Int, bossBar: Boolean,
    private val actionOnTimeUp: () -> Unit) {

    private var remainingSeconds :Int
    private val totalSecond : Int
    private val gameTimeBar : BossBar?

    private var task: BukkitTask? = null
    private val showSecondsThreshold = 120
    init{
        gameTimeBar = if(!bossBar) null
        else Bukkit.createBossBar("Time Left", BarColor.GREEN, BarStyle.SOLID)
        totalSecond = seconds
        remainingSeconds = seconds

        updateBossBar()
    }



    fun resume(){
        if(task != null) return
        if(gameTimeBar != null){
            gameTimeBar.progress = remainingSeconds.toDouble()/totalSecond.toDouble()
            gameTimeBar.color = if(remainingSeconds > showSecondsThreshold) BarColor.GREEN else BarColor.YELLOW
            for (player in Bukkit.getOnlinePlayers())
                gameTimeBar.addPlayer(player)
        }

        task = object: BukkitRunnable() {
            override fun run() {
                if (remainingSeconds <= 0) {
                    actionOnTimeUp.invoke()
                    cancel() // Stop the timer when time is up
                } else {
                    remainingSeconds--
                    updateBossBar()
                }
            }
        }.runTaskTimer(plugin, 20L, 20L)
    }
    fun pause(){
        task?.cancel()
        task = null
    }

    fun clear(){
        gameTimeBar?.removeAll()
        task?.cancel()
        task = null
    }

    private fun updateBossBar() {
        if(gameTimeBar == null) return
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val title = if (remainingSeconds <= showSecondsThreshold) {
            String.format("%d minute%s, %02d second%s", minutes, if (minutes != 1) "s" else "", seconds, if (seconds != 1) "s" else "")
        } else "$minutes minute${if (minutes != 1) "s" else ""}"
        gameTimeBar.progress = remainingSeconds.toDouble() / totalSecond.toDouble()
        gameTimeBar.color = if (remainingSeconds > showSecondsThreshold) BarColor.GREEN else BarColor.YELLOW
        gameTimeBar.setTitle("Time Left: $title")
    }
}
