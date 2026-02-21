package awa.hyw.Axolotl.util

import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.TextColor
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.Axolotl.isNull
import awa.hyw.Axolotl.Axolotl.mc

object ChatUtil {

    fun addMessageWithClient(message: String) {
        if (isNull()) return

        val clientName = Axolotl.NAME
        val prefix: MutableComponent = Component.empty()
        val startColor = Axolotl.GRADIENT_START
        val endColor = Axolotl.GRADIENT_END
        
        for (i in clientName.indices) {
            val step = i.toFloat() / (clientName.length - 1).coerceAtLeast(1)
            val color = getGradientColor(startColor, endColor, step)
            prefix.append(Component.literal(clientName[i].toString()).withStyle { it.withColor(TextColor.fromRgb(color)) })
        }
        
        prefix.append(Component.literal(" >>> ").withStyle { it.withColor(TextColor.fromRgb(0xCCCCCC)) })
        prefix.append(Component.literal(message))
        
        mc.gui.chat.addMessage(prefix)
    }

    private fun getGradientColor(startColor: Int, endColor: Int, step: Float): Int {
        val r1 = (startColor ushr 24) and 0xFF
        val g1 = (startColor ushr 16) and 0xFF
        val b1 = (startColor ushr 8) and 0xFF
        
        val r2 = (endColor ushr 24) and 0xFF
        val g2 = (endColor ushr 16) and 0xFF
        val b2 = (endColor ushr 8) and 0xFF
        
        val r = (r1 + (r2 - r1) * step).toInt()
        val g = (g1 + (g2 - g1) * step).toInt()
        val b = (b1 + (b2 - b1) * step).toInt()
        
        return (r shl 16) or (g shl 8) or b
    }
}

