package awa.hyw.Axolotl.gui.widget.impl.setting

import org.lwjgl.glfw.GLFW
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.multidraw.FontMultiDraw
import awa.hyw.Axolotl.graphics.multidraw.PosColor2DMultiDraw
import awa.hyw.Axolotl.gui.GuiScreen
import awa.hyw.Axolotl.gui.widget.AbstractSettingWidget
import awa.hyw.Axolotl.settings.KeyBindSetting
import awa.hyw.Axolotl.util.input.KeyBind

class KeybindSettingWidget(
    screen: GuiScreen,
    override val setting: KeyBindSetting
) : AbstractSettingWidget(screen, setting) {
    private var binding = false

    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        fontMulti.addText(
            "${setting.name.translation}: " +
                    if (binding) "Press a key to bind"
                    else if (setting.value.keyCode == -1) "Not bound"
                    else "Bound to ${setting.value.keyName.uppercase()}",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        binding = !binding
    }

    override fun onWindowClose() {
        binding = false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        if (!binding) return false

        val kc = if (scanCode == 310) GLFW.GLFW_KEY_RIGHT_SHIFT else keyCode
        val sc = scanCode

        if (kc == GLFW.GLFW_KEY_ESCAPE) {
            binding = false
            return true
        }

        binding = false
        if (kc == GLFW.GLFW_KEY_DELETE) {
            setting.value = KeyBind(KeyBind.Type.KEYBOARD, -1, -1)
            return true
        }

        setting.value = KeyBind(KeyBind.Type.KEYBOARD, kc, sc)
        return true
    }
}
