package awa.hyw.Axolotl.module.impl.combat

import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.PostTickEvent
import awa.hyw.Axolotl.module.Module
import net.minecraft.client.KeyMapping
import java.lang.reflect.Field
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object AutoClicker : Module(
    name = "AutoClicker",
    category = Category.COMBAT
) {
    private val leftClick by setting("Left Click", true)
    private val rightClick by setting("Right Click", false)
    private val breakCheck by setting("Pause on Block Break", true)
    private val cooldown by setting("Attack Cooldown", false) { leftClick }

    private val minCps by setting("Min CPS", 8, 1..60)
    private val maxCps by setting("Max CPS", 12, 1..60)

    private var nextLeftClickAtMs = 0L
    private var nextRightClickAtMs = 0L
    private var isDestroyingField: Field? = null

    override fun onDisable() {
        nextLeftClickAtMs = 0L
        nextRightClickAtMs = 0L
    }

    @EventTarget
    fun onTick(event: PostTickEvent) {
        if (mc.player == null || mc.level == null) return
        if (mc.screen != null) return
        if (breakCheck && isBreakingBlock()) return

        val now = System.currentTimeMillis()

        nextLeftClickAtMs = if (leftClick && mc.options.keyAttack.isDown) {
            runClicker(now, nextLeftClickAtMs, mc.options.keyAttack, true)
        } else {
            0L
        }

        nextRightClickAtMs = if (rightClick && mc.options.keyUse.isDown) {
            runClicker(now, nextRightClickAtMs, mc.options.keyUse, false)
        } else {
            0L
        }
    }

    private fun runClicker(now: Long, nextAt: Long, key: KeyMapping, isLeft: Boolean): Long {
        if (isLeft && cooldown) {
            if (mc.player!!.getAttackStrengthScale(0.5f) < 0.95f) {
                return now // Wait for cooldown
            }
        }

        val minCps0 = min(minCps, maxCps).coerceAtLeast(1)
        val maxCps0 = max(minCps, maxCps).coerceAtLeast(minCps0)

        // Prevent burst clicking if lagged behind
        var next = if (nextAt <= 0L || now - nextAt > 500) now else nextAt

        while (now >= next) {
            KeyMapping.click(key.key)
            val cps = if (minCps0 == maxCps0) {
                minCps0.toDouble()
            } else {
                Random.nextDouble(minCps0.toDouble(), maxCps0.toDouble() + 1e-6)
            }
            val delayMs = (1000.0 / cps).toLong().coerceAtLeast(1L)
            next += delayMs
        }
        return next
    }

    private fun isBreakingBlock(): Boolean {
        val gameMode = mc.gameMode ?: return false
        
        if (isDestroyingField == null) {
            try {
                // Try SRG name first
                isDestroyingField = gameMode.javaClass.getDeclaredField("f_105196_")
            } catch (e: NoSuchFieldException) {
                try {
                    // Try mapped name
                    isDestroyingField = gameMode.javaClass.getDeclaredField("isDestroying")
                } catch (e2: Exception) {
                    // If both fail, log error and return false (avoid crash)
                    // e2.printStackTrace()
                    return false
                }
            }
            isDestroyingField?.isAccessible = true
        }

        return try {
            isDestroyingField?.getBoolean(gameMode) ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
