package awa.hyw.Axolotl.module.impl.misc

import awa.hyw.Axolotl.module.Module
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket
import net.minecraft.network.protocol.game.ClientboundSetScorePacket
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import java.util.concurrent.ConcurrentHashMap

object HealthBypass : Module(
    name = "HealthBypass",
    category = Category.MISC
) {
    private val healthByOwner = ConcurrentHashMap<String, Int>()

    override fun onDisable() {
        healthByOwner.clear()
    }

    fun onPacket(packet: Packet<*>): Boolean {
        if (!enabled) return false
        val player = mc.player ?: return false

        if (packet is ClientboundSetScorePacket) {
            val objective = packet.objectiveName
            if (objective != "belowHealth" && objective != "health") return false

            val owner = packet.owner
            if (owner == player.scoreboardName) return false

            healthByOwner[owner] = packet.score
            return false
        }

        if (packet is ClientboundSetHealthPacket) {
            return packet.health > 20.0f
        }

        return false
    }

    fun getHealth(entity: LivingEntity): Float {
        if (!enabled) return entity.health
        if (entity !is Player) return entity.health

        val value = healthByOwner[entity.scoreboardName] ?: return entity.health
        return value.toFloat()
    }

    fun getMaxHealth(entity: LivingEntity): Float {
        if (!enabled) return entity.maxHealth
        if (entity !is Player) return entity.maxHealth

        if (!healthByOwner.containsKey(entity.scoreboardName)) return entity.maxHealth
        return 20.0f
    }
}

