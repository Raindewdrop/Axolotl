package awa.hyw.Axolotl.module.impl.render

import org.lwjgl.glfw.GLFW
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.gui.clickgui.DropdownClickGUI
import awa.hyw.Axolotl.module.Module

object ClickGUI : Module(
    name = "ClickGui",
    category = Category.RENDER,
    defaultKeybind = GLFW.GLFW_KEY_RIGHT_SHIFT,
) {
    val themeColor by setting("Theme Color", ColorRGB(Axolotl.GRADIENT_START))
    val themeColorEnd by setting("Theme Color End", ColorRGB(Axolotl.GRADIENT_END))

    val saveCfgOnCloseGui by setting("save-cfg-on-close-gui", true)

    override fun onEnable() {
        if (mc.player == null) {
            enabled = false
            return
        }
        DropdownClickGUI.openScreen()
        logger.info("ClickGUI enabled")
    }

    override fun onLoad() {
        enabled = false
        super.onLoad()
    }
}