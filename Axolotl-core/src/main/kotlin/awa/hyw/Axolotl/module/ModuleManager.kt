package awa.hyw.Axolotl.module

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap
import net.minecraft.client.gui.screens.ChatScreen
import org.lwjgl.glfw.GLFW
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.KeyEvent
import awa.hyw.Axolotl.event.impl.Render2DEvent
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.easing.AnimationFlag
import awa.hyw.Axolotl.graphics.easing.Easing
import awa.hyw.Axolotl.graphics.utils.RenderUtils2D
import awa.hyw.Axolotl.module.impl.combat.*
import awa.hyw.Axolotl.module.impl.render.*
import awa.hyw.Axolotl.module.impl.misc.*
import awa.hyw.Axolotl.settings.KeyBindSetting
import awa.hyw.Axolotl.util.input.KeyBind
import java.util.concurrent.ConcurrentHashMap

object ModuleManager {
    private val modulesMap = Object2ObjectAVLTreeMap<String, Module>()

    private val fadeAnimations = ConcurrentHashMap<String, AnimationFlag>()
    private val mc by lazy { Axolotl.mc }

    private var draggingHUD: RenderableModule? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var clickGuiMouseDown = false

    fun init() {
        addModule(Animations)
        addModule(ClickGUI)
        addModule(ItemScale)
        addModule(Hitboxes)
        addModule(Particles)
        addModule(FullBright)
        addModule(MotionBlur)
        addModule(WaterMark)
        addModule(ArrayListMod)
        addModule(ESP)
        addModule(HealthBypass)
        addModule(AutoClicker)
        addModule(AimAssist)

        modules().forEach { it.onLoad() }
    }

    private fun addModule(module: Module) {
        modulesMap[module.name] = module
    }

    operator fun get(name: String): Module {
        return modulesMap[name]!!
    }

    fun getNullable(name: String): Module? {
        return modulesMap[name] ?: modulesMap.values.find { it.name.equals(name, ignoreCase = true) }
    }

    fun modules(): List<Module> {
        return modulesMap.values.toList()
    }

    /* render stuff */
    @EventTarget
    fun onRender2d(event: Render2DEvent) {
        val window = mc.window.window
        val xpos = DoubleArray(1)
        val ypos = DoubleArray(1)

        run {
            val keyBind = ClickGUI.keyBind
            val isDown = keyBind.keyCode != -1 && GLFW.glfwGetKey(window, keyBind.keyCode) == GLFW.GLFW_PRESS
            val wasDown = clickGuiMouseDown
            clickGuiMouseDown = isDown

            if (mc.screen == null && !ClickGUI.enabled && isDown && !wasDown) {
                ClickGUI.toggle()
            }
        }

        GLFW.glfwGetCursorPos(window, xpos, ypos)
        val mouseX = xpos[0].toFloat() / mc.window.guiScale.toFloat()
        val mouseY = ypos[0].toFloat() / mc.window.guiScale.toFloat()

        modules()
            .filterIsInstance<RenderableModule>()
            .filter { it.enabled }
            .forEach { hud ->
                hud.render(event)

                if (mc.screen is ChatScreen) {
                    val fadeAnimation = fadeAnimations.getOrPut(hud.name) {
                        AnimationFlag(Easing.OUT_CUBIC, 300f)
                    }

                    val targetFade = if (hud.isMouseOver(mouseX, mouseY)) 1f else 0f
                    fadeAnimation.update(targetFade)
                    val currentFade = fadeAnimation.get()
                    if (currentFade > 0) {
                        drawHUDBorder(hud, currentFade)
                    }
                }
            }

        if (mc.screen is ChatScreen) {
            onMouseInput(mouseX, mouseY)
        }
    }

    private fun onMouseInput(mouseX: Float, mouseY: Float) {
        val window = mc.window.window
        val isMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
        val screenWidth = mc.window.guiScaledWidth.toFloat()
        val screenHeight = mc.window.guiScaledHeight.toFloat()

        if (isMousePressed) {
            if (draggingHUD == null) {
                modules()
                    .filterIsInstance<RenderableModule>()
                    .firstOrNull { it.enabled && it.isMouseOver(mouseX, mouseY) }
                    ?.let { hud ->
                        draggingHUD = hud
                        dragOffsetX = mouseX - hud.x
                        dragOffsetY = mouseY - hud.y
                    }
            } else {
                val newX = mouseX - dragOffsetX
                val newY = mouseY - dragOffsetY
                val hud = draggingHUD!!

                if (newX >= 0 && newX + hud.width <= screenWidth) {
                    hud.x = newX
                }
                if (newY >= 0 && newY + hud.height <= screenHeight) {
                    hud.y = newY
                }

                drawHUDBorder(hud, 1f)
            }
        } else {
            draggingHUD = null
        }
    }

    private fun drawHUDBorder(hud: RenderableModule, alpha: Float) {
        val borderColor = ColorRGB(255, 255, 255, (150 * alpha).toInt())
        val outlineWidth = 1f

        RenderUtils2D.drawRectOutline(
            hud.x - outlineWidth,
            hud.y - outlineWidth,
            hud.width + (outlineWidth * 2),
            hud.height + (outlineWidth * 2),
            borderColor
        )
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        if (event.action == GLFW.GLFW_PRESS) {
            var keyCode = event.keyCode
            val scanCode = event.scanCode

            if (scanCode == 310) {
                keyCode = GLFW.GLFW_KEY_RIGHT_SHIFT
            }

            if ((keyCode != -1 && ClickGUI.keyBind.keyCode == keyCode) ||
                (scanCode != -1 && ClickGUI.keyBind.scanCode == scanCode)
            ) {
                if (mc.screen != null && mc.screen!!.title.string == "Axolotl-ClickGUI") {
                    return
                }
                ClickGUI.toggle()
                event.isCancelled = true
                return
            }

            if (mc.screen != null) return

            modules()
                .filter { it !== ClickGUI }
                .forEach { module ->
                    if ((keyCode != -1 && module.keyBind.keyCode == keyCode) ||
                        (scanCode != -1 && module.keyBind.scanCode == scanCode)
                    ) {
                        module.toggle()
                        event.isCancelled = true
                    }
                }

            modules()
                .filter { it.enabled }
                .forEach { module ->
                    module.settings.forEach { setting ->
                        if (setting is KeyBindSetting) {
                            if ((keyCode != -1 && setting.value.keyCode == keyCode) ||
                                (scanCode != -1 && setting.value.scanCode == scanCode)) {
                                setting.method()
                            }
                        }
                    }
                }
        }
    }
}
