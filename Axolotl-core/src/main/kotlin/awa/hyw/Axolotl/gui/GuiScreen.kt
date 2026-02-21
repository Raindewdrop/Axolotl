package awa.hyw.Axolotl.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.event.EventBus
import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.Render2DEvent

abstract class GuiScreen(val name: String) {
    private var mouseX = 0f
    private var mouseY = 0f
    private var screen: Screen? = null

    val mc by lazy { Axolotl.mc }

    open fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) {}
    open fun mouseClicked(buttonId: Int, mouseX: Float, mouseY: Float) {}
    open fun mouseReleased(buttonId: Int, mouseX: Float, mouseY: Float) {}
    open fun mouseScrolled(mouseX: Float, mouseY: Float, scrollAmount: Float): Boolean {
        return false
    }

    open fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        return false
    }

    open fun charTyped(char: Char, modifiers: Int): Boolean {
        return false
    }

    open fun shouldCloseOnEsc(): Boolean = true
    open fun onClose() {}

    fun openScreen() {
        if (screen == null) {
            screen = object : Screen(Component.literal("Axolotl-$name")) {
                override fun shouldCloseOnEsc() = true
                override fun isPauseScreen() = false

                override fun init() {
                    super.init()
                    val window = Axolotl.mc.window.window
                    org.lwjgl.glfw.GLFW.glfwSetInputMode(window, org.lwjgl.glfw.GLFW.GLFW_CURSOR, org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL)
                }

                override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
                    super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
                    mouseX = pMouseX.toFloat()
                    mouseY = pMouseY.toFloat()
                }

                @EventTarget
                fun onRender2D(event: Render2DEvent) {
                    drawScreen(mouseX, mouseY, event.partialTick)
                }

                override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
                    mouseClicked(pButton, pMouseX.toFloat(), pMouseY.toFloat())
                    return true
                }

                override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
                    mouseReleased(pButton, pMouseX.toFloat(), pMouseY.toFloat())
                    return true
                }

                override fun keyPressed(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
                    return keyPressed(pKeyCode, pScanCode) ||
                            super.keyPressed(pKeyCode, pScanCode, pModifiers)
                }

                override fun charTyped(pCodePoint: Char, pModifiers: Int): Boolean {
                    return this@GuiScreen.charTyped(pCodePoint, pModifiers) || super.charTyped(pCodePoint, pModifiers)
                }

                override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pScrollY: Double): Boolean {
                    return mouseScrolled(pMouseX.toFloat(), pMouseY.toFloat(), pScrollY.toFloat()) ||
                            super.mouseScrolled(pMouseX, pMouseY, pScrollY)
                }

                override fun onClose() {
                    super.onClose()
                    closeScreen()
                }
            }
        }
        screen?.let { EventBus.register(it) }
        mc.setScreen(screen)
    }

    fun closeScreen() {
        onClose()
        screen?.let { EventBus.unregister(it) }
        if (mc.screen?.title == screen?.title) mc.setScreen(null)
    }
}

