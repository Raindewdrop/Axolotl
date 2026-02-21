package awa.hyw.Axolotl.gui.widget

import awa.hyw.Axolotl.graphics.multidraw.FontMultiDraw
import awa.hyw.Axolotl.graphics.multidraw.PosColor2DMultiDraw
import awa.hyw.Axolotl.gui.GuiScreen
import awa.hyw.Axolotl.settings.AbstractSetting

abstract class AbstractSettingWidget(
    screen: GuiScreen,
    open val setting: AbstractSetting<*>
): AbstractWidget(screen) {
    open fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
    }

    final override fun draw(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        if (!setting.visibility()) return
        draw0(screenWidth, screenHeight, mouseX, mouseY,
            renderX, renderY, fontMulti, rectMulti, partialTicks)
    }

    final override fun isVisible(): Boolean {
        return setting.visibility()
    }

    override fun getHeight(): Float = 20f
}

