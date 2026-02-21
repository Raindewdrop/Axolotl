package awa.hyw.Axolotl.gui.widget.impl.setting

import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.multidraw.FontMultiDraw
import awa.hyw.Axolotl.graphics.multidraw.PosColor2DMultiDraw
import awa.hyw.Axolotl.gui.GuiScreen
import awa.hyw.Axolotl.gui.widget.AbstractSettingWidget
import awa.hyw.Axolotl.settings.NumberSetting
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class NumberSettingWidget<N: Number>(screen: GuiScreen, override val setting: NumberSetting<N>): AbstractSettingWidget(screen, setting) {
    private var dragging = false
    private var mouseX = -1f
    private var mouseY = -1f

    @Suppress("UNCHECKED_CAST")
    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        this.mouseX = mouseX
        this.mouseY = mouseY
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)

        if (dragging && mouseX in renderX..(renderX + (screenWidth - 2 * 5f))) {
            val v = (mouseX - renderX) / (screenWidth - 2 * 5f) * setting.maxValue.toFloat()
            setting.value(closestMultiple(v, setting.step.toFloat()) as N)
        }

        rectMulti.addRect(
            renderX, renderY,
            (screenWidth - 2 * 5f) * (setting.value.toFloat() / setting.maxValue.toFloat()), getHeight(),
            ColorRGB(0.5f, 0.5f, 0.5f).alpha(0.4f)
        )
        fontMulti.addText(
            "${setting.name.translation} : ${(setting.value.toFloat() * 100).roundToInt() / 100f}",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )
    }

    private fun closestMultiple(target: Float, multiple: Float): Number {
        val lowerMultiple = floor((target / multiple)) * multiple
        val upperMultiple = ceil((target / multiple)) * multiple

        return if (target - lowerMultiple <= upperMultiple - target) lowerMultiple else upperMultiple
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        dragging = true
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, isLeftClick: Boolean) : Boolean {
        if (dragging) {
            dragging = false
            return true
        }
        return false
    }
}

