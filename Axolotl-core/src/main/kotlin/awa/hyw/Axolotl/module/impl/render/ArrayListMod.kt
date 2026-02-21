package awa.hyw.Axolotl.module.impl.render

import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.Render2DEvent
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.easing.AnimationFlag
import awa.hyw.Axolotl.graphics.easing.Easing
import awa.hyw.Axolotl.graphics.font.FontRenderers
import awa.hyw.Axolotl.graphics.utils.RenderUtils2D
import awa.hyw.Axolotl.module.Module
import awa.hyw.Axolotl.module.ModuleManager
import awa.hyw.Axolotl.settings.*
import java.awt.Color

object ArrayListMod : Module(
    name = "ArrayList",
    category = Category.RENDER
) {
    private var rainbow by setting("Rainbow", true)
    private var saturation by setting("Saturation", 0.8f, 0f..1f, 0.1f) { rainbow }
    private var brightness by setting("Brightness", 1f, 0f..1f, 0.1f) { rainbow }
    private var speed by setting("Speed", 2f, 0.1f..10f, 0.1f) { rainbow }
    private var offset by setting("Offset", 200f, 0f..1000f, 10f) { rainbow }
    
    private var customColor by setting("Color", ColorRGB.WHITE) { !rainbow }
    
    private var shadow by setting("Shadow", true)
    private var background by setting("Background", true)
    private var backgroundAlpha by setting("BackgroundAlpha", 0.5f, 0f..1f, 0.05f) { background }
    private var animTime by setting("AnimTime", 300f, 0f..1000f, 50f)

    private val moduleAnimations = HashMap<Module, AnimationFlag>()

    init {
        hidden.value = true
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val allModules = ModuleManager.modules()
        val screenWidth = Axolotl.mc.window.guiScaledWidth.toFloat()
        
        allModules.forEach { module ->
            if (module.enabled && !module.hidden.value) {
                getAnimation(module).update(1f)
            } else {
                getAnimation(module).update(0f)
            }
        }

        val visibleModules = allModules
            .filter { getAnimation(it).get() > 0.01f }
            .sortedByDescending { FontRenderers.getStringWidth(it.getDisplayName()) }

        var y = 2f
        var count = 0

        visibleModules.forEach { module ->
            val anim = getAnimation(module)
            val animValue = anim.get()
            
            val name = module.getDisplayName()
            val textWidth = FontRenderers.getStringWidth(name)
            val height = FontRenderers.getHeight() + 2f
            
            val targetX = screenWidth - textWidth - 2f
            val offScreenX = screenWidth + 2f
            val x = offScreenX + (targetX - offScreenX) * animValue
            
            val color = if (rainbow) {
                getRainbowColor(count * offset.toInt())
            } else {
                customColor
            }

            if (background) {
                RenderUtils2D.drawRectFilled(
                    x - 2f, 
                    y - 1f, 
                    textWidth + 4f, 
                    height, 
                    ColorRGB(0f, 0f, 0f, backgroundAlpha)
                )
            }

            FontRenderers.drawString(name, x, y, color, shadow)
            
            y += height * animValue
            count++
        }
    }
    
    private fun getAnimation(module: Module): AnimationFlag {
        return moduleAnimations.getOrPut(module) { 
            AnimationFlag(Easing.OUT_CUBIC, animTime) 
        }
    }

    private fun getRainbowColor(offset: Int): ColorRGB {
        val hue = (System.currentTimeMillis() * speed + offset) % 2000 / 2000f
        val c = Color.getHSBColor(hue, saturation, brightness)
        return ColorRGB(c.red, c.green, c.blue, c.alpha)
    }
}
