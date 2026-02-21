package awa.hyw.Axolotl.gui.widget.impl.setting

import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.multidraw.FontMultiDraw
import awa.hyw.Axolotl.graphics.multidraw.PosColor2DMultiDraw
import awa.hyw.Axolotl.gui.GuiScreen
import awa.hyw.Axolotl.gui.widget.AbstractSettingWidget
import awa.hyw.Axolotl.settings.BooleanSetting

class BooleanSettingWidget(screen: GuiScreen, override val setting: BooleanSetting): AbstractSettingWidget(screen, setting) {
    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        fontMulti.addText(
            setting.name.translation,
            renderX + 2f,
            renderY + 3f,
            if (setting.value) ColorRGB.GREEN else ColorRGB.RED
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        setting.toggle()
    }
}

