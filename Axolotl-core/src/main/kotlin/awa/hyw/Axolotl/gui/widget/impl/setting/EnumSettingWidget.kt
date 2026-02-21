package awa.hyw.Axolotl.gui.widget.impl.setting

import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.multidraw.FontMultiDraw
import awa.hyw.Axolotl.graphics.multidraw.PosColor2DMultiDraw
import awa.hyw.Axolotl.gui.GuiScreen
import awa.hyw.Axolotl.gui.widget.AbstractSettingWidget
import awa.hyw.Axolotl.settings.EnumSetting
import awa.hyw.Axolotl.translation.DirectTranslationEnum
import awa.hyw.Axolotl.translation.TranslationEnum
import awa.hyw.Axolotl.translation.TranslationManager
import awa.hyw.Axolotl.translation.TranslationString

class EnumSettingWidget<E: Enum<E>>(screen: GuiScreen, override val setting: EnumSetting<E>):
    AbstractSettingWidget(screen, setting) {
    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        val valueName = when (setting.value) {
            is TranslationEnum -> TranslationString(setting.name.key.fullKey, (setting.value as TranslationEnum).keyString).translation
            is DirectTranslationEnum -> TranslationManager.getTranslation((setting.value as DirectTranslationEnum).keyString)
            else -> setting.value.name
        }

        fontMulti.addText(
            "${setting.name.translation}: $valueName",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        setting.forwardLoop()
    }
}

