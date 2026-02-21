package awa.hyw.Axolotl.graphics.font

import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.graphics.color.ColorRGB
import java.awt.Font

object FontRenderers {
    val fontRendererType = FontRendererType.GENERAL

    const val DRAW_FONT_SIZE = 12f

    const val FONT_SIZE = 16f

    // Comfortaa
    private val enFont = kotlin.runCatching {
        FontAdapter(
            Font.createFont(
                Font.TRUETYPE_FONT,
                javaClass.getResourceAsStream("${Axolotl.ASSETS_DIRECTORY}/font/font.ttf")
            ).deriveFont(Font.PLAIN, FONT_SIZE * 2f)
        )
    }.onFailure { e -> e.printStackTrace() }.getOrThrow()

    internal val default = FontRenderer(enFont)

    fun drawString(
        text: String, x: Float, y: Float,
        color: ColorRGB, shadow: Boolean = false,
        scale: Float = 1.0f
    ): Float =
        default.drawString(text, x, y, color, shadow, scale)

    fun drawStringRev(
        text: String, x: Float, y: Float,
        color: ColorRGB, shadow: Boolean = false,
        scale: Float = 1.0f
    ): Float =
        default.drawStringRev(text, x, y, color, shadow, scale)

    fun getStringWidth(text: String, scale: Float = 1.0f): Float =
        default.getStringWidth(text, scale)

    fun getHeight(scale: Float = 1.0f): Float =
        default.getHeight(scale)

}

