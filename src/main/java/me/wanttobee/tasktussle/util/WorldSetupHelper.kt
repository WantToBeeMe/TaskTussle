package me.wanttobee.tasktussle.util

import me.wanttobee.tasktussle.MinecraftPlugin
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


// This class is used to set up the border and saturation effect for everyone for example
// and to remove it again when you actually start the game
object WorldSetupHelper : Listener {
    private const val BORDER_RANGE = 15.0
    private var lobbyActive = false
    private var lobbyWorldBorder: WorldBorder? = null
    private var lobbyCenter : Location? = null // This center is needed, since world border-center does not hold the Y value

    private var parkourActive = false
    private val replacedBlocks: MutableMap<Location, Material> = mutableMapOf()
    private val playersReachedGoal: MutableSet<Player> = mutableSetOf()

    // The parkour does not have anything to do with the lobby itself.
    // A parkour can only be created when the lobby is active and can only be destroyed when the lobby is active
    fun setUpParkour(invoker: Player? = null){
        if (!lobbyActive){
            invoker?.sendMessage("${ChatColor.RED}Lobby not active, cannot setup parkour")
            return
        }

        if (parkourActive)
            tearDownParkour(invoker)

        val world = invoker?.world ?: return

        // Replace random blocks with cyan stained-glass
        val random = java.util.Random()
        for (i in 1..50) { // Replace 50 blocks as an example
            val x = lobbyCenter!!.blockX + random.nextInt(12) - 6
            val z = lobbyCenter!!.blockZ + random.nextInt(12) - 6
            val y = lobbyCenter!!.blockY + random.nextInt(5)
            val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
            val block = world.getBlockAt(location)

            if (!replacedBlocks.containsKey(location)) {
                replacedBlocks[location] = block.type
                block.type = Material.LIME_STAINED_GLASS
            }
        }
        for (i in 1..500) { // Replace 50 blocks as an example
            val x = lobbyCenter!!.blockX + random.nextInt(16) - 8
            val z = lobbyCenter!!.blockZ + random.nextInt(16) - 8
            val y = lobbyCenter!!.blockY + random.nextInt(30) + 5
            val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())
            val block = world.getBlockAt(location)

            if (!replacedBlocks.containsKey(location)) {
                replacedBlocks[location] = block.type
                block.type = Material.CYAN_STAINED_GLASS
            }
        }

        val ringY = lobbyCenter!!.blockY + 35
        val ringRadius = 7
            // Create the ring of obsidian
        for (angle in 0 until 360 step 10) { // Create the ring in 10-degree increments
            val radians = Math.toRadians(angle.toDouble())
            val x = lobbyCenter!!.blockX + (ringRadius * Math.cos(radians)).toInt()
            val z = lobbyCenter!!.blockZ + (ringRadius * Math.sin(radians)).toInt()
            val location = Location(world, x.toDouble(), ringY.toDouble(), z.toDouble())
            val block = world.getBlockAt(location)

            if (!replacedBlocks.containsKey(location)) {
                replacedBlocks[location] = block.type
                block.type = Material.BLACK_STAINED_GLASS
            }
        }

        val borderPadding = 8
        for (x in (lobbyCenter!!.blockX - borderPadding)..(lobbyCenter!!.blockX + borderPadding)) {
            for (z in (lobbyCenter!!.blockZ - borderPadding)..(lobbyCenter!!.blockZ + borderPadding)) {
                val distanceSquared = ((x - lobbyCenter!!.blockX) * (x - lobbyCenter!!.blockX) +
                        (z - lobbyCenter!!.blockZ) * (z - lobbyCenter!!.blockZ))
                if (distanceSquared > (ringRadius * ringRadius)-4) { // Outside the ring
                    val location = Location(world, x.toDouble(), ringY.toDouble(), z.toDouble())
                    val block = world.getBlockAt(location)

                    if (!replacedBlocks.containsKey(location)) {
                        replacedBlocks[location] = block.type
                        block.type = Material.BLACK_STAINED_GLASS
                    }
                }
            }
        }
        parkourActive = true
    }

    fun tearDownParkour(invoker: Player? = null){
        if (!parkourActive || !lobbyActive){
            invoker?.sendMessage("${ChatColor.RED}Parkour not active, cannot remove parkour")
            return
        }

        playersReachedGoal.clear()

        // Restore original blocks
        for ((location, material) in replacedBlocks) {
            val block = location.world?.getBlockAt(location)
            if (block != null) {
                block.type = material
            }
        }
        replacedBlocks.clear()

        applyToAllPlayers { player ->
            teleportPlayerToLobbyCenter(player)
        }

        parkourActive = false
    }

    // It is safe to recall this method.
    // It wil just replace the location of the border, that's it
    fun startLobby(invoker: Player) {
        lobbyActive = true
        lobbyCenter = invoker.location
        applyToAllPlayers { player ->
            applyLobbyEffectsToPlayer(player)
            teleportPlayerToLobbyCenter(player)
        }

        invoker.gameMode = GameMode.CREATIVE



        lobbyWorldBorder = invoker.world.worldBorder
        lobbyWorldBorder?.apply {
            center = invoker.location
            size = BORDER_RANGE
        }

        invoker.world.setSpawnLocation(invoker.location.blockX, invoker.location.blockY, invoker.location.blockZ)


        // There is probably a better way, but I don't feel like looking it up, so I don't care :)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set 0")
    }

    fun clearLobby(invoker: Player) {
        if (!lobbyActive)
            return

        tearDownParkour(invoker)
        lobbyActive = false

        applyToAllPlayers { player ->
            removeLobbyEffectsFromPlayer(player)
        }

        lobbyWorldBorder?.apply {
            size = 30000000.0
        }
        lobbyWorldBorder = null
        lobbyCenter = null

        // There is probably a better way, but I don't feel like looking it up, so I don't care :)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "time set 0")
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "weather clear")
    }

    private fun applyLobbyEffectsToPlayer(player: Player) {
        player.addPotionEffect(
            PotionEffect(PotionEffectType.SATURATION, Int.MAX_VALUE, 10, true, false, false),
        )
        player.gameMode = GameMode.ADVENTURE
        player.inventory.clear()
    }

    private fun removeLobbyEffectsFromPlayer(player: Player) {
        player.activePotionEffects.forEach { effect -> player.removePotionEffect(effect.type) }

        player.gameMode = GameMode.SURVIVAL
        player.inventory.clear()
    }


    private fun applyToAllPlayers(action: (Player) -> Unit) {
        for (player in MinecraftPlugin.instance.server.onlinePlayers) {
            action(player)
        }
    }

    private fun teleportPlayerToLobbyCenter(player: Player) {
        // Instead of teleporting them all to center,
        // we teleport them to the center with a random offset between -1 and 1 in the x and z direction
        val random = java.util.Random()
        val offsetX = random.nextDouble() * 2 - 1
        val offsetZ = random.nextDouble() * 2 - 1
        val targetLocation = lobbyCenter!!.clone().add(offsetX, 0.0, offsetZ)
        player.teleport(targetLocation)
    }

    @EventHandler
    fun onPlayerLeave(event : EntityDamageEvent){
        // players cant die in the lobby
        if (lobbyActive && event.entity is Player){
            val player = event.entity as Player

            if (player.health - event.finalDamage <= 0){
                event.isCancelled = true
                player.health = 20.0

                player.world.spawnParticle(
                    Particle.TOTEM_OF_UNDYING, player.location,
                    30, 0.5, 1.5, 0.5, 0.1)
                player.world.playSound(player.location, Sound.ITEM_TOTEM_USE, 0.7f, 1.0f)
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

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!parkourActive)
            return

        val player = event.player
        val to = event.to ?: return
        val blockBelow = to.clone().subtract(0.0, 1.0, 0.0).block

        // Check if the block below is black stained glass
        if (blockBelow.type == Material.BLACK_STAINED_GLASS) {
            // Check if the player is already in the set
            if (!playersReachedGoal.contains(player)) {
                // Broadcast the message
                Bukkit.broadcastMessage("${ChatColor.GOLD}${player.name} has reached the top!")

                // Add the player to the set
                playersReachedGoal.add(player)
            }
        }
    }
}