package awa.hyw.Axolotl.module.impl.render

import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.event.impl.Render2DEvent
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.font.FontRenderers
import awa.hyw.Axolotl.module.RenderableModule
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

object WaterMark: RenderableModule(
    name = "WaterMark",
    category = Category.RENDER,
    defaultX = 5f,
    defaultY = 5f,
    width = FontRenderers.getStringWidth(Axolotl.NAME)
) {
    private val shadow by setting("shadow", false)
    private val scale by setting("scale", 1f, 1f..5f, 0.1f)
    private val glow by setting("glow", true)
    private val glowRadius by setting("glow-radius", 2f, 1f..5f, 0.5f) { glow }
    private val glowAlpha by setting("glow-alpha", 100, 0..255, 1) { glow }
    private val showVersion by setting("show-version", true)

    override fun render(event: Render2DEvent) {
        val text = Axolotl.NAME
        val versionText = Axolotl.VERSION
        val nameWidth = FontRenderers.getStringWidth(text, scale)
        val versionScale = scale * 0.7f
        val versionWidth = if (showVersion) FontRenderers.getStringWidth(versionText, versionScale) + 2f else 0f
        
        width = nameWidth + versionWidth
        height = FontRenderers.getHeight(scale)

        val startColor = Axolotl.GRADIENT_START
        val endColor = Axolotl.GRADIENT_END

        if (glow) {
            var glowX = x
            for (i in text.indices) {
                val step = i.toFloat() / (text.length - 1).coerceAtLeast(1)
                val colorInt = getGradientColor(startColor, endColor, step)
                val charStr = text[i].toString()

                drawGlowText(charStr, glowX, y, ColorRGB(colorInt), scale)

                glowX += FontRenderers.getStringWidth(charStr, scale)
            }
            
            if (showVersion) {
                val nameHeight = FontRenderers.getHeight(scale)
                val versionHeight = FontRenderers.getHeight(versionScale)
                val versionY = y + (nameHeight - versionHeight)
                val versionX = x + nameWidth + 2f

                drawGlowText(versionText, versionX, versionY, ColorRGB(170, 170, 170), versionScale)
            }
        }

        var currentX = x
        for (i in text.indices) {
            val step = i.toFloat() / (text.length - 1).coerceAtLeast(1)
            val color = getGradientColor(startColor, endColor, step)
            val charStr = text[i].toString()
            FontRenderers.drawString(charStr, currentX, y, ColorRGB(color), shadow, scale)
            currentX += FontRenderers.getStringWidth(charStr, scale)
        }
        
        if (showVersion) {
            val nameHeight = FontRenderers.getHeight(scale)
            val versionHeight = FontRenderers.getHeight(versionScale)
            val versionY = y + (nameHeight - versionHeight)
            FontRenderers.drawString(versionText, x + nameWidth + 2f, versionY, ColorRGB(170, 170, 170), shadow, versionScale)
        }
    }

    private fun getGradientColor(startColor: Int, endColor: Int, step: Float): Int {
        val r1 = (startColor ushr 24) and 0xFF
        val g1 = (startColor ushr 16) and 0xFF
        val b1 = (startColor ushr 8) and 0xFF
        val a1 = startColor and 0xFF

        val r2 = (endColor ushr 24) and 0xFF
        val g2 = (endColor ushr 16) and 0xFF
        val b2 = (endColor ushr 8) and 0xFF
        val a2 = endColor and 0xFF

        val r = (r1 + (r2 - r1) * step).toInt()
        val g = (g1 + (g2 - g1) * step).toInt()
        val b = (b1 + (b2 - b1) * step).toInt()
        val a = (a1 + (a2 - a1) * step).toInt()

        return (r shl 24) or (g shl 16) or (b shl 8) or a
    }

    private fun drawGlowText(text: String, x: Float, y: Float, color: ColorRGB, scale: Float) {
        val layers = ceil(glowRadius * 2f).toInt().coerceAtLeast(1)
        val samples = 16
        for (layer in 1..layers) {
            val t = layer.toFloat() / layers.toFloat()
            val r = glowRadius * t
            val a = (glowAlpha * (1f - t) * (1f - t)).toInt().coerceIn(0, 255)
            if (a <= 0) continue
            val layerColor = color.alpha(a)
            for (i in 0 until samples) {
                val angle = (PI * 2.0) * (i.toDouble() / samples.toDouble())
                val ox = (cos(angle) * r.toDouble()).toFloat()
                val oy = (sin(angle) * r.toDouble()).toFloat()
                FontRenderers.drawString(text, x + ox, y + oy, layerColor, false, scale)
            }
        }
    }
}
