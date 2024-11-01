package me.wanttobee.tasktussle.util

import me.wanttobee.commandtree.Description
import me.wanttobee.commandtree.ITreeCommand
import me.wanttobee.commandtree.partials.BranchPartial
import me.wanttobee.commandtree.partials.EmptyPartial
import me.wanttobee.commandtree.partials.ICommandPartial
import org.bukkit.Bukkit
import org.bukkit.WorldBorder
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent


// This class is used to set up the border and saturation effect for everyone for example
// and to remove it again when you actually start the game
object StartHelper : ITreeCommand, Listener {
    override val description = Description("used to set the border and saturation effect and stuff like that")
        .addSubDescription("start", "to start the lobby thing")
        .addSubDescription("clear", "to clear the lobby thing")

    override val command: ICommandPartial = BranchPartial("lobby").setStaticPartials(
        EmptyPartial("start").setEffect { invoker -> startLobby(invoker) },
        EmptyPartial("clear").setEffect { invoker -> clearLobby(invoker) },
    )

    private var lobbyActive = false
    private var lobbyWorldBorder: WorldBorder? = null

    fun startLobby(invoker: Player) {
        lobbyActive = true
        for (player in invoker.server.onlinePlayers) {
            applyLobbyEffectsToPlayer(player)
            player.teleport(invoker.location)
        }

        invoker.gameMode = org.bukkit.GameMode.CREATIVE

        lobbyWorldBorder = invoker.world.worldBorder
        lobbyWorldBorder?.apply {
            center = invoker.location
            size = 15.0
        }

        invoker.world.setSpawnLocation(invoker.location.blockX, invoker.location.blockY, invoker.location.blockZ)


        // There is probably a better way, but I don't feel like looking it up, so I don't care :)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set 0")
    }
    fun clearLobby(invoker: Player) {
        lobbyActive = false
        for (player in invoker.server.onlinePlayers) {
            removeLobbyEffectsFromPlayer(player)
        }

        lobbyWorldBorder?.apply {
            size = 30000000.0
        }
        lobbyWorldBorder = null

        // There is probably a better way, but I don't feel like looking it up, so I don't care :)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set 0")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather clear")
    }

    private fun applyLobbyEffectsToPlayer(player: Player) {
        player.addPotionEffect(org.bukkit.potion.PotionEffect(
            org.bukkit.potion.PotionEffectType.SATURATION, Int.MAX_VALUE, 10, true, false, false)
        )
        player.gameMode = org.bukkit.GameMode.ADVENTURE
        player.inventory.clear()
    }
    private fun removeLobbyEffectsFromPlayer(player: Player) {
        player.activePotionEffects.forEach { effect -> player.removePotionEffect(effect.type) }

        player.gameMode = org.bukkit.GameMode.SURVIVAL
        player.inventory.clear()
    }


    @EventHandler
    fun onPlayerLeave(event : EntityDamageEvent){
        // players cant die in the lobby
        if (lobbyActive && event.entity is Player){
            val player = event.entity as Player

            if (player.health - event.finalDamage <= 0){
                event.isCancelled = true
                player.health = 20.0

                player.world.spawnParticle(org.bukkit.Particle.TOTEM_OF_UNDYING, player.location,
                    30, 0.5, 1.5, 0.5, 0.1)
                player.world.playSound(player.location, org.bukkit.Sound.ITEM_TOTEM_USE, 0.7f, 1.0f)
            }
        }
    }

    @EventHandler
    fun onPlayerLeave(event : PlayerQuitEvent){
        // We remove the effect, to prevent player that leave when the effect is being removed when they are not in the lobby
        // that they still have the effect
        if (lobbyActive)
            removeLobbyEffectsFromPlayer(event.player)
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (lobbyActive) {
            applyLobbyEffectsToPlayer(event.player)

            if (lobbyWorldBorder != null) {
                val border = lobbyWorldBorder!!
                val player = event.player
                val playerLocation = player.location

                val borderCenter = border.center
                val borderSize = border.size / 2

                val x = playerLocation.x
                val z = playerLocation.z

                val xDiff = x - borderCenter.x
                val zDiff = z - borderCenter.z

                val outsideBorder = (xDiff > borderSize || xDiff < -borderSize || zDiff > borderSize || zDiff < -borderSize);
                if (!outsideBorder)
                    return

                var previousGoodLocation = borderCenter
                for (y in 255 downTo 0) {
                    val checkLocation = borderCenter.clone()
                    checkLocation.y = y.toDouble()
                    if (player.world.getBlockAt(checkLocation).type.isAir)
                        previousGoodLocation = checkLocation
                    else{
                        player.teleport(previousGoodLocation)
                        return
                    }
                }
            }
        }
    }
}