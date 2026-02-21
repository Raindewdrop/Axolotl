package awa.hyw.Axolotl.module.impl.combat

import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.Render3DEvent
import awa.hyw.Axolotl.module.Module
import awa.hyw.Axolotl.util.extensions.distanceTo
import awa.hyw.Axolotl.util.math.MathUtil
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.max

object AimAssist : Module(
    name = "AimAssist",
    category = Category.COMBAT
) {
    private val speed by setting("Speed", 5.0, 1.0..100.0)
    private val range by setting("Range", 4.0, 1.0..10.0)
    private val fov by setting("FOV", 90.0, 10.0..360.0)
    private val players by setting("Players", true)
    private val mobs by setting("Mobs", false)
    private val animals by setting("Animals", false)
    private val invisibles by setting("Invisibles", false)
    private val clickOnly by setting("Click Only", true)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.player == null || mc.level == null) return
        if (mc.screen != null) return
        if (clickOnly && !mc.options.keyAttack.isDown) return

        val target = getTarget(event.partialTicks) ?: return
        aimAt(target, event.partialTicks)
    }

    private fun getTarget(partialTicks: Float): LivingEntity? {
        val level = mc.level as? ClientLevel ?: return null
        val entities = level.entitiesForRendering()
        var bestTarget: LivingEntity? = null
        var minDistance = Double.MAX_VALUE

        for (entity in entities) {
            if (entity !is LivingEntity) continue
            if (entity == mc.player) continue
            if (!isValid(entity)) continue

            val distance = mc.player!!.distanceTo(entity).toDouble()
            if (distance > range) continue
            
            // Check FOV
            val rotations = getRotations(entity, partialTicks)
            val yawDiff = abs(wrapAngleTo180(rotations[0] - mc.player!!.yRot))
            if (yawDiff > fov / 2) continue

            // Select closest target by distance
            // Alternatively, could sort by angle difference
            if (distance < minDistance) {
                minDistance = distance
                bestTarget = entity
            }
        }
        return bestTarget
    }

    private fun isValid(entity: LivingEntity): Boolean {
        if (entity.isDeadOrDying) return false
        if (entity.isInvisible && !invisibles) return false
        if (entity is Player && players) return true
        if (entity is Monster && mobs) return true
        if (entity is Animal && animals) return true
        return false
    }

    private fun aimAt(target: LivingEntity, partialTicks: Float) {
        val rotations = getRotations(target, partialTicks)
        val currentYaw = MathUtil.lerp(mc.player!!.yRotO, mc.player!!.yRot, partialTicks)
        val currentPitch = MathUtil.lerp(mc.player!!.xRotO, mc.player!!.xRot, partialTicks)
        
        val targetYaw = rotations[0]
        val targetPitch = rotations[1]

        val yawDiff = wrapAngleTo180(targetYaw - currentYaw)
        val pitchDiff = wrapAngleTo180(targetPitch - currentPitch)

        // Adjust speed for frame rate independence (approximate)
        // Default speed was tuned for 20 TPS, now running at FPS (e.g. 60-144+)
        // Simple heuristic: speed / 10 per frame
        val speedVal = speed.toFloat() / 2f
        
        // Apply smoothing based on speed
        var yawChange = MathUtil.clamp(yawDiff, -speedVal, speedVal)
        var pitchChange = MathUtil.clamp(pitchDiff, -speedVal, speedVal)

        // GCD Fix - Snap to mouse sensitivity grid
        val sensitivity = getSensitivity() * 0.6 + 0.2
        val gcd = sensitivity * sensitivity * sensitivity * 1.2
        val gcdVal = gcd * 8.0 

        if (yawChange != 0f) {
            yawChange = (Math.round(yawChange / gcdVal) * gcdVal).toFloat()
        }
        if (pitchChange != 0f) {
            pitchChange = (Math.round(pitchChange / gcdVal) * gcdVal).toFloat()
        }

        mc.player!!.yRot += yawChange
        mc.player!!.xRot += pitchChange
    }

    private fun getSensitivity(): Double {
        return try {
            val field = mc.options.javaClass.getDeclaredField("sensitivity")
            field.isAccessible = true
            val option = field.get(mc.options)
            // Assuming OptionInstance has a get() method or similar
            // Since we can't easily access OptionInstance class to check methods if it's generic
            // We'll try to reflectively call 'get'
            val getMethod = option.javaClass.getMethod("get")
            getMethod.isAccessible = true
            getMethod.invoke(option) as Double
        } catch (e: Exception) {
            // Try fallback field name if mapping changed
            try {
                // In some mappings it might be 'gamma' or something else, but let's stick to safe fallback
                0.5
            } catch (ignored: Exception) {
                0.5
            }
        }
    }

    private fun getRotations(entity: Entity, partialTicks: Float): FloatArray {
        // Interpolate entity position for smoother tracking
        val entX = MathUtil.lerp(entity.xo, entity.x, partialTicks.toDouble())
        val entY = MathUtil.lerp(entity.yo, entity.y, partialTicks.toDouble())
        val entZ = MathUtil.lerp(entity.zo, entity.z, partialTicks.toDouble())

        val myX = MathUtil.lerp(mc.player!!.xo, mc.player!!.x, partialTicks.toDouble())
        val myY = MathUtil.lerp(mc.player!!.yo, mc.player!!.y, partialTicks.toDouble())
        val myZ = MathUtil.lerp(mc.player!!.zo, mc.player!!.z, partialTicks.toDouble())

        val diffX = entX - myX
        val diffZ = entZ - myZ
        // Aim at eye height approximately
        val diffY = (entY + entity.eyeHeight * 0.85) - (myY + mc.player!!.eyeHeight)
        
        val dist = sqrt(diffX * diffX + diffZ * diffZ)
        
        val yaw = (Math.toDegrees(atan2(diffZ, diffX)) - 90.0).toFloat()
        val pitch = (-Math.toDegrees(atan2(diffY, dist))).toFloat()
        
        return floatArrayOf(yaw, pitch)
    }

    private fun wrapAngleTo180(angle: Float): Float {
        var a = angle % 360.0f
        if (a >= 180.0f) a -= 360.0f
        if (a < -180.0f) a += 360.0f
        return a
    }
}
