package awa.hyw.Axolotl.gui.clickgui

import org.lwjgl.glfw.GLFW
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.graphics.ScissorBox
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.color.ColorUtils
import awa.hyw.Axolotl.graphics.easing.AnimationFlag
import awa.hyw.Axolotl.graphics.easing.Easing
import awa.hyw.Axolotl.graphics.multidraw.FontMultiDraw
import awa.hyw.Axolotl.graphics.multidraw.PosColor2DMultiDraw
import awa.hyw.Axolotl.gui.GuiScreen
import awa.hyw.Axolotl.module.Module
import awa.hyw.Axolotl.module.ModuleManager
import awa.hyw.Axolotl.module.impl.render.ClickGUI
import awa.hyw.Axolotl.gui.window.DragWindow
import awa.hyw.Axolotl.config.ConfigManager
import awa.hyw.Axolotl.settings.*
import kotlin.math.abs
import kotlin.math.roundToInt

class DropdownClickGUI : GuiScreen("ClickGUI") {
    private val panels = mutableListOf<CategoryPanel>()
    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()
    private val scissorBox = ScissorBox()
    
    var activeTextSetting: TextSetting? = null
    var activeNumberSetting: NumberSetting<*>? = null
    var numberInputBuffer = ""
    
    private var globalScrollY = 0f
    private var globalScrollVelocity = 0f

    init {
        var xOffset = 20f
        Module.Category.entries.forEach { category ->
            panels.add(CategoryPanel(category, xOffset, 20f))
            xOffset += 110f
        }
    }

    override fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) {
        val window = Axolotl.mc.window.window
        org.lwjgl.glfw.GLFW.glfwSetInputMode(window, org.lwjgl.glfw.GLFW.GLFW_CURSOR, org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL)
        
        // Global scroll logic
        if (abs(globalScrollVelocity) > 0.01f) {
            globalScrollY += globalScrollVelocity
            globalScrollVelocity *= 0.92f
            if (abs(globalScrollVelocity) < 0.05f) globalScrollVelocity = 0f
        }
        panels.forEach { it.draw(mouseX, mouseY, partialTicks, fontMulti, rectMulti, globalScrollY) }
        rectMulti.draw()
        fontMulti.draw()
    }

    override fun mouseClicked(buttonId: Int, mouseX: Float, mouseY: Float) {
        if (activeNumberSetting != null && !isHoveringNumberSetting(mouseX, mouseY)) {
            applyActiveNumberSetting()
            activeNumberSetting = null
        }
        activeTextSetting = null
        for (panel in panels.asReversed()) {
            if (panel.mouseClicked(mouseX, mouseY, buttonId, globalScrollY)) return
        }
    }

    private fun isHoveringNumberSetting(mouseX: Float, mouseY: Float): Boolean {
        return false 
    }

    private fun applyActiveNumberSetting() {
        activeNumberSetting?.let { setting ->
            try {
                val str = numberInputBuffer
                if (str.isNotEmpty() && str != "-") {
                    val num = str.toDouble()
                    val clamped = num.coerceIn(setting.minValue.toDouble(), setting.maxValue.toDouble())
                    
                    when (setting) {
                        is IntSetting -> setting.value(clamped.toInt())
                        is FloatSetting -> setting.value(clamped.toFloat())
                        is DoubleSetting -> setting.value(clamped)
                        is LongSetting -> setting.value(clamped.toLong())
                    }
                }
            } catch (e: NumberFormatException) {
                // Ignore
            }
        }
    }

    override fun mouseReleased(buttonId: Int, mouseX: Float, mouseY: Float) {
        panels.forEach { it.mouseReleased(mouseX, mouseY, buttonId) }
    }

    override fun mouseScrolled(mouseX: Float, mouseY: Float, scrollAmount: Float): Boolean {
        globalScrollVelocity += scrollAmount * 5f
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        val kc = if (scanCode == 310) GLFW.GLFW_KEY_RIGHT_SHIFT else keyCode
        val sc = scanCode

        if (activeTextSetting != null) {
            when (kc) {
                GLFW.GLFW_KEY_BACKSPACE -> {
                    if (activeTextSetting!!.value.isNotEmpty()) {
                        activeTextSetting!!.value = activeTextSetting!!.value.dropLast(1)
                    }
                    return true
                }
                GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE -> {
                    activeTextSetting = null
                    return true
                }
            }
        }

        if (activeNumberSetting != null) {
            when (kc) {
                GLFW.GLFW_KEY_BACKSPACE -> {
                    if (numberInputBuffer.isNotEmpty()) {
                        numberInputBuffer = numberInputBuffer.dropLast(1)
                    }
                    return true
                }
                GLFW.GLFW_KEY_ENTER -> {
                    applyActiveNumberSetting()
                    activeNumberSetting = null
                    return true
                }
                GLFW.GLFW_KEY_ESCAPE -> {
                    activeNumberSetting = null
                    return true
                }
            }
        }

        if (panels.any { it.keyPressed(kc, sc) }) return true

        if ((kc != -1 && kc == ClickGUI.keyBind.keyCode) ||
            (sc != -1 && sc == ClickGUI.keyBind.scanCode) ||
            kc == GLFW.GLFW_KEY_ESCAPE
        ) {
            closeScreen()
            return true
        }

        return super.keyPressed(kc, sc)
    }

    override fun charTyped(char: Char, modifiers: Int): Boolean {
        if (activeTextSetting != null) {
            if (char.code >= 32) {
                activeTextSetting!!.value += char
                return true
            }
        }
        if (activeNumberSetting != null) {
            if (char.isDigit() || char == '.' || char == '-') {
                numberInputBuffer += char
                return true
            }
        }
        return super.charTyped(char, modifiers)
    }

    override fun onClose() {
        ClickGUI.enabled = false
        if (ClickGUI.saveCfgOnCloseGui) ConfigManager.saveAllConfig()
        val window = Axolotl.mc.window.window
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED)
    }

    inner class CategoryPanel(val category: Module.Category, x: Float, y: Float) {
        private val dragWindow = DragWindow(x, y, 100f, 20f)
        private var expanded = true
        private val modules = ModuleManager.modules().filter { it.category == category }.map { ModuleButton(it) }
        
        private val expandAnim = AnimationFlag(Easing.OUT_CUBIC, 300f)
        private var scrollOffset = 0f
        private var scrollVelocity = 0f

        private fun guiHeight(): Float {
            val window = Axolotl.mc.window
            return window.height / window.guiScale.toFloat()
        }

        private fun headerHeight(): Float = 20f

        private fun viewportHeight(): Float {
            val maxByScreen = (guiHeight() - dragWindow.y - 25f).coerceAtLeast(60f)
            return minOf(280f, maxByScreen)
        }

        private fun contentHeight(): Float {
            return modules.sumOf { it.getRenderHeight().toDouble() }.toFloat()
        }

        fun draw(mouseX: Float, mouseY: Float, partialTicks: Float, fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw, globalScroll: Float) {
            val drawX = dragWindow.x
            val drawY = dragWindow.y + globalScroll
            
            dragWindow.update(mouseX, mouseY - globalScroll) // Correct drag logic
            
            // Re-sync draw position after drag update
            val finalDrawX = dragWindow.x
            val finalDrawY = dragWindow.y + globalScroll

            rectMulti.addRectGradientHorizontal(
                finalDrawX, finalDrawY, dragWindow.width, headerHeight(),
                ClickGUI.themeColor, ClickGUI.themeColorEnd
            )
            fontMulti.addText(category.getDisplayName(), finalDrawX + 5f, finalDrawY + 4f, ColorRGB.WHITE, false, 1f)
            rectMulti.draw()
            fontMulti.draw()

            expandAnim.update(if (expanded) 1f else 0f)
            val animProgress = expandAnim.get()

            val contentHeight = contentHeight()
            // No viewport/scroll limit here anymore

            val contentVisibleHeight = contentHeight * animProgress
            val contentX = finalDrawX
            val contentY = finalDrawY + headerHeight()

            if (contentVisibleHeight > 0.5f) {
                scissorBox.updateAndDraw(contentX, contentY, dragWindow.width, contentVisibleHeight) {
                    var currentY = contentY // No internal scroll offset
                    modules.forEach { moduleButton ->
                        currentY = moduleButton.draw(contentX, currentY, dragWindow.width, mouseX, mouseY, partialTicks, fontMulti, rectMulti)
                    }
                    rectMulti.draw()
                    fontMulti.draw()
                }
            }
            
            // Note: dragWindow.height is used for hit testing usually, update it
            dragWindow.height = headerHeight() + contentVisibleHeight
        }

        fun mouseClicked(mouseX: Float, mouseY: Float, buttonId: Int, globalScroll: Float): Boolean {
            val drawY = dragWindow.y + globalScroll
            
            // Header click check
            if (mouseX >= dragWindow.x && mouseX <= dragWindow.x + dragWindow.width &&
                mouseY >= drawY && mouseY <= drawY + headerHeight()) {
                
                if (buttonId == 0) {
                    // Start drag with offset correction
                    dragWindow.startDrag(mouseX, mouseY - globalScroll)
                } else if (buttonId == 1) {
                    expanded = !expanded
                }
                return true
            }

            if (expanded) {
                val contentX = dragWindow.x
                val contentY = drawY + headerHeight()
                val contentHeight = contentHeight()
                
                // Hit test on modules
                if (mouseX >= contentX && mouseX <= contentX + dragWindow.width &&
                    mouseY >= contentY && mouseY <= contentY + contentHeight) {
                    
                    var currentY = contentY
                    for (moduleButton in modules) {
                        val height = moduleButton.getRenderHeight()
                        if (moduleButton.mouseClicked(mouseX, mouseY, buttonId, contentX, currentY, dragWindow.width)) return true
                        currentY += height
                    }
                }
            }
            return false
        }

        fun mouseReleased(mouseX: Float, mouseY: Float, buttonId: Int) {
            if (buttonId == 0) dragWindow.stopDrag()
            if (expanded) {
                modules.forEach { it.mouseReleased(mouseX, mouseY, buttonId) }
            }
        }

        fun mouseScrolled(mouseX: Float, mouseY: Float, scrollAmount: Float): Boolean {
            if (!expanded) return false
            val contentX = dragWindow.x
            val contentY = dragWindow.y + headerHeight()
            val viewportHeight = viewportHeight()
            val contentHeight = contentHeight()
            if (contentHeight <= viewportHeight) return false

            if (mouseX !in contentX..(contentX + dragWindow.width) ||
                mouseY !in contentY..(contentY + viewportHeight)
            ) return false

            scrollVelocity += (-scrollAmount) * 12f
            scrollVelocity = scrollVelocity.coerceIn(-60f, 60f)
            return true
        }

        fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
            if (!expanded) return false
            return modules.any { it.keyPressed(keyCode, scanCode) }
        }
    }

    inner class ModuleButton(val module: Module) {
        private var expanded = false
        private var x = 0f
        private var y = 0f
        private var width = 0f
        private var draggingSlider: NumberSetting<*>? = null
        private var bindingModule = false
        private var bindingSetting: KeyBindSetting? = null
        
        // Color Picker State
        private var activeColorSetting: ColorSetting? = null
        private var pickerExpanded = false
        private val colorPickerAnim = AnimationFlag(Easing.OUT_CUBIC, 200f)
        
        private var draggingAlpha = false
        private var draggingHue = false
        private var draggingColor = false
        private var colorPickerArea: Area? = null
        private var hueBarArea: Area? = null
        private var alphaBarArea: Area? = null
        
        private var lastClickTime = 0L
        private var lastClickSetting: AbstractSetting<*>? = null
        
        private val expandAnim = AnimationFlag(Easing.OUT_CUBIC, 300f)
        private val settingRowHeight = 16f
        private val moduleHeaderHeight = 18f

        fun getRenderHeight(): Float {
            expandAnim.update(if (expanded) 1f else 0f)
            val animProgress = expandAnim.get()
            
            colorPickerAnim.update(if (pickerExpanded) 1f else 0f)
            
            val visibleSettings = module.settings.filter { it.visibility() }
            var settingsHeight = 0f
            
            for (setting in visibleSettings) {
                settingsHeight += settingRowHeight
                if (setting is ColorSetting && activeColorSetting == setting) {
                    val pickerHeight = getPickerHeight(width)
                    settingsHeight += pickerHeight * colorPickerAnim.get()
                }
            }
            
            return moduleHeaderHeight + settingsHeight * animProgress
        }

        private fun getPickerHeight(width: Float): Float {
            val pickerPadding = 4f
            val pickerSize = minOf(70f, (width - 2 * pickerPadding - 28f).coerceAtLeast(40f))
            return pickerPadding + pickerSize + pickerPadding
        }

        fun draw(x: Float, y: Float, width: Float, mouseX: Float, mouseY: Float, partialTicks: Float, fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw): Float {
            rectMulti.addRect(x, y, width, moduleHeaderHeight, 
                if (module.enabled) ColorRGB(0.2f, 0.2f, 0.2f) else ColorRGB(0.12f, 0.12f, 0.12f))
            
            if (module.enabled) {
                rectMulti.addRect(x, y, 2f, moduleHeaderHeight, ClickGUI.themeColorEnd)
            }

            val nameText = if (bindingModule) "${module.getDisplayName()}..." else module.getDisplayName()
            fontMulti.addText(nameText, x + 7f, y + 4f,
                if (module.enabled) ColorRGB.WHITE else ColorRGB.GRAY, false, 0.9f)
            
            if (module.settings.any { it.visibility() }) {
                fontMulti.addText(if (expanded) "-" else "+", x + width - 12f, y + 4f, ColorRGB.GRAY, false, 0.9f)
            }
            rectMulti.draw()
            fontMulti.draw()

            expandAnim.update(if (expanded) 1f else 0f)
            val animProgress = expandAnim.get()
            
            var currentY = y + moduleHeaderHeight
            
            if (animProgress > 0.01f) {
                colorPickerAnim.update(if (pickerExpanded) 1f else 0f)
                val pickerAnimValue = colorPickerAnim.get()
                
                val visibleSettings = module.settings.filter { it.visibility() }

                var fullSettingsHeight = 0f
                visibleSettings.forEach { setting ->
                    fullSettingsHeight += settingRowHeight
                    if (setting is ColorSetting && activeColorSetting == setting) {
                        fullSettingsHeight += getPickerHeight(width) * pickerAnimValue
                    }
                }
                
                val clipHeight = fullSettingsHeight * animProgress
                
                if (clipHeight > 0.5f) {
                    // Use scissor for nice expansion
                    scissorBox.updateAndDraw(x, currentY, width, clipHeight) { 
                        var settingY = currentY
                        visibleSettings.forEach { setting ->
                            settingY = drawSetting(setting, x, settingY, width, mouseX, mouseY, fontMulti, rectMulti, animProgress, pickerAnimValue)
                        }
                        rectMulti.draw()
                        fontMulti.draw()
                    }
                }
                currentY += clipHeight
            }
            
            return currentY
        }

        @Suppress("UNCHECKED_CAST")
        private fun drawSetting(
            setting: AbstractSetting<*>,
            x: Float,
            y: Float,
            width: Float,
            mouseX: Float,
            mouseY: Float,
            fontMulti: FontMultiDraw,
            rectMulti: PosColor2DMultiDraw,
            alpha: Float,
            pickerAnim: Float
        ): Float {
            rectMulti.addRect(x, y, width, settingRowHeight, ColorRGB(0.05f, 0.05f, 0.05f).alpha(alpha))
            rectMulti.addRect(x, y, 2f, settingRowHeight, ColorRGB(0.15f, 0.15f, 0.15f).alpha(alpha))
            
            when (setting) {
                is BooleanSetting -> {
                    fontMulti.addText(setting.name.translation, x + 10f, y + 4f, 
                        (if (setting.value) ColorRGB.WHITE else ColorRGB.GRAY).alpha(alpha), false, 0.8f)
                    return y + settingRowHeight
                }
                is NumberSetting<*> -> {
                    val progress = (setting.value.toFloat() - setting.minValue.toFloat()) / (setting.maxValue.toFloat() - setting.minValue.toFloat())
                    rectMulti.addRect(x + 5f, y + 12f, (width - 10f) * progress, 2f, ClickGUI.themeColorEnd.alpha(alpha))
                    
                    if (activeNumberSetting == setting) {
                        fontMulti.addText("${setting.name.translation}: $numberInputBuffer", x + 10f, y + 2f, ColorRGB.WHITE.alpha(alpha), false, 0.8f)
                    } else {
                        fontMulti.addText("${setting.name.translation}: ${setting.value}", x + 10f, y + 2f, ColorRGB.LIGHT_GRAY.alpha(alpha), false, 0.8f)
                    }
                    
                    if (draggingSlider == setting && activeNumberSetting != setting) {
                        var newValue = ((mouseX - (x + 5f)) / (width - 10f) * (setting.maxValue.toFloat() - setting.minValue.toFloat()) + setting.minValue.toFloat())
                            .coerceIn(setting.minValue.toFloat(), setting.maxValue.toFloat())
                        
                        // Apply step
                        val step = setting.step.toDouble()
                        if (step > 0) {
                            val steps = ((newValue - setting.minValue.toFloat()) / step).roundToInt()
                            newValue = (setting.minValue.toDouble() + (steps * step)).toFloat()
                        }
                        
                        newValue = newValue.coerceIn(setting.minValue.toFloat(), setting.maxValue.toFloat())

                        when (setting) {
                            is IntSetting -> setting.value(newValue.roundToInt())
                            is FloatSetting -> setting.value(newValue)
                            is DoubleSetting -> setting.value(newValue.toDouble())
                            is LongSetting -> setting.value(newValue.toLong())
                        }
                    }
                    return y + settingRowHeight
                }
                is EnumSetting<*> -> {
                    fontMulti.addText("${setting.name.translation}: ${setting.value}", x + 10f, y + 4f, ColorRGB.LIGHT_GRAY.alpha(alpha), false, 0.8f)
                    return y + settingRowHeight
                }
                is ColorSetting -> {
                    fontMulti.addText(setting.name.translation, x + 10f, y + 4f, ColorRGB.LIGHT_GRAY.alpha(alpha), false, 0.8f)
                    rectMulti.addRect(x + width - 15f, y + 4f, 8f, 8f, setting.value.alpha(alpha))

                    var nextY = y + settingRowHeight
                    
                    if (activeColorSetting == setting && pickerAnim > 0.01f) {
                        val pickerHeight = getPickerHeight(width)
                        val currentPickerHeight = pickerHeight * pickerAnim
                        
                        // Scissor for picker animation if needed, but we already scissor the whole list.
                        // However, we need to handle the layout.
                        
                        val pickerPadding = 4f
                        val pickerSize = minOf(70f, (width - 2 * pickerPadding - 28f).coerceAtLeast(40f))
                        val pickerX = x + pickerPadding
                        val pickerY = nextY + pickerPadding
                        
                        // Draw background for picker
                        rectMulti.addRect(x, nextY, width, currentPickerHeight, ColorRGB(0.08f, 0.08f, 0.08f).alpha(alpha))

                        if (currentPickerHeight > 10f) { // Only draw contents if visible enough
                            colorPickerArea = Area(pickerX, pickerY, pickerSize, pickerSize)
                            hueBarArea = Area(pickerX + pickerSize + 6f, pickerY, 10f, pickerSize)
                            alphaBarArea = Area(pickerX + pickerSize + 6f + 10f + 6f, pickerY, 10f, pickerSize)

                            if (pickerExpanded) { // Only update if expanded (not closing)
                                if (draggingAlpha) updateAlphaFromMouse(setting, mouseY)
                                else if (draggingHue) updateHueFromMouse(setting, mouseY)
                                else if (draggingColor) updateColorFromPicker(setting, mouseX, mouseY)
                            }

                            drawColorPicker(setting, rectMulti, alpha * pickerAnim)
                            drawHueBar(setting, rectMulti, alpha * pickerAnim)
                            drawAlphaBar(setting, rectMulti, alpha * pickerAnim)
                        }

                        nextY += currentPickerHeight
                    }

                    return nextY
                }
                is KeyBindSetting -> {
                    val text = if (bindingSetting == setting) {
                        "${setting.name.translation}: Press a key"
                    } else {
                        "${setting.name.translation}: ${setting.value.keyName}"
                    }
                    fontMulti.addText(text, x + 10f, y + 4f, ColorRGB.LIGHT_GRAY.alpha(alpha), false, 0.8f)
                    return y + settingRowHeight
                }
                is TextSetting -> {
                    rectMulti.addRect(x + 5f, y + 4f, width - 10f, 10f, ColorRGB(0.02f, 0.02f, 0.02f).alpha(alpha))
                    if (activeTextSetting == setting) {
                        rectMulti.addRect(x + 5f, y + 13f, width - 10f, 1f, ClickGUI.themeColorEnd.alpha(alpha))
                    }
                    val display = if (activeTextSetting == setting) setting.value + "_" else setting.value
                    fontMulti.addText("${setting.name.translation}: $display", x + 7f, y + 6f, ColorRGB.LIGHT_GRAY.alpha(alpha), false, 0.7f)
                    return y + settingRowHeight
                }
                else -> {
                    fontMulti.addText(setting.name.translation, x + 10f, y + 4f, ColorRGB.LIGHT_GRAY.alpha(alpha), false, 0.8f)
                    return y + settingRowHeight
                }
            }
        }

        fun mouseClicked(mouseX: Float, mouseY: Float, buttonId: Int, x: Float, y: Float, width: Float): Boolean {
            // Check header click
            if (mouseX in x..(x + width) && mouseY in y..(y + moduleHeaderHeight)) {
                if (buttonId == 0) {
                    module.toggle()
                } else if (buttonId == 1) {
                    expanded = !expanded
                } else if (buttonId == 2) {
                    bindingModule = !bindingModule // Toggle binding mode? Or just bind next key?
                    // Usually binding is handled via KeyBindSetting or separate logic.
                    // This seems to be "Bind Module Key" logic.
                    // But wait, DropdownClickGUI usually has binding in settings.
                    // The existing code had this logic.
                }
                return true
            }

            if (!expanded) return false

            val animProgress = expandAnim.get()
            if (animProgress <= 0.01f) return false

            val visibleSettings = module.settings.filter { it.visibility() }
            var currentY = y + moduleHeaderHeight
            val pickerAnimValue = colorPickerAnim.get()
            
            // Check settings clicks
            visibleSettings.forEach { setting ->
                var rowHeight = settingRowHeight
                if (setting is ColorSetting && activeColorSetting == setting) {
                    rowHeight += getPickerHeight(width) * pickerAnimValue
                }
                
                // We need to check if click is within the visible (clipped) area if animating?
                // But usually we just check logic.
                // However, we need to pass the correct Y to handleSettingClick
                
                if (mouseY >= currentY && mouseY <= currentY + rowHeight) {
                     handleSettingClick(setting, buttonId, mouseX, mouseY, x, currentY, width)
                     return true
                }
                
                currentY += rowHeight
            }
            return false
        }

        private fun handleSettingClick(
            setting: AbstractSetting<*>,
            buttonId: Int,
            mouseX: Float,
            mouseY: Float,
            x: Float,
            y: Float,
            width: Float
        ) {
            when (setting) {
                is BooleanSetting -> setting.value = !setting.value
                is EnumSetting<*> -> {
                    if (buttonId == 0) setting.forwardLoop()
                }
                is NumberSetting<*> -> {
                    if (buttonId == 0) {
                        val now = System.currentTimeMillis()
                        if (lastClickSetting == setting && now - lastClickTime < 300) {
                            activeNumberSetting = setting
                            numberInputBuffer = setting.value.toString()
                            draggingSlider = null
                        } else {
                            draggingSlider = setting
                        }
                        lastClickSetting = setting
                        lastClickTime = now
                    }
                }
                is TextSetting -> {
                    if (buttonId == 0) activeTextSetting = setting
                }
                is KeyBindSetting -> {
                    if (buttonId == 0) {
                        bindingSetting = if (bindingSetting == setting) null else setting
                        bindingModule = false
                    }
                }
                is ColorSetting -> {
                    val inRow = mouseY <= y + settingRowHeight
                    if (buttonId == 0 && inRow) { // Left click to open/close picker
                        if (activeColorSetting == setting) {
                            pickerExpanded = !pickerExpanded
                        } else {
                            activeColorSetting = setting
                            pickerExpanded = true
                        }
                        draggingAlpha = false
                        draggingHue = false
                        draggingColor = false
                    }
                    
                    if (activeColorSetting == setting && pickerExpanded) {
                        val picker = colorPickerArea
                        val hueBar = hueBarArea
                        val alphaBar = alphaBarArea

                        when {
                            alphaBar?.contains(mouseX, mouseY) == true -> draggingAlpha = true
                            hueBar?.contains(mouseX, mouseY) == true -> draggingHue = true
                            picker?.contains(mouseX, mouseY) == true -> {
                                draggingColor = true
                                updateColorFromPicker(setting, mouseX, mouseY)
                            }
                        }
                    }
                }
            }
        }

        fun mouseReleased(mouseX: Float, mouseY: Float, buttonId: Int) {
            if (buttonId == 0) {
                draggingSlider = null
                draggingAlpha = false
                draggingHue = false
                draggingColor = false
            }
        }

        fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
            if (!bindingModule && bindingSetting == null) return false

            if (bindingModule) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    bindingModule = false
                    return true
                }
                if (keyCode == GLFW.GLFW_KEY_DELETE) {
                    module.keyBind = awa.hyw.Axolotl.util.input.KeyBind(awa.hyw.Axolotl.util.input.KeyBind.Type.KEYBOARD, -1, -1)
                    bindingModule = false
                    return true
                }

                // Use scanCode from event directly to avoid native crash
                module.keyBind = awa.hyw.Axolotl.util.input.KeyBind(awa.hyw.Axolotl.util.input.KeyBind.Type.KEYBOARD, keyCode, scanCode)
                bindingModule = false
                return true
            }

            val binding = bindingSetting
            if (binding != null) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    bindingSetting = null
                    return true
                }
                if (keyCode == GLFW.GLFW_KEY_DELETE) {
                    binding.value = awa.hyw.Axolotl.util.input.KeyBind(awa.hyw.Axolotl.util.input.KeyBind.Type.KEYBOARD, -1, -1)
                    bindingSetting = null
                    return true
                }
                
                // Use scanCode from event directly to avoid native crash
                binding.value = awa.hyw.Axolotl.util.input.KeyBind(awa.hyw.Axolotl.util.input.KeyBind.Type.KEYBOARD, keyCode, scanCode)
                bindingSetting = null
                return true
            }

            return false
        }

        private fun getSettingHeight(setting: AbstractSetting<*>): Float {
            // Deprecated: logic moved to getRenderHeight and drawSetting
            return settingRowHeight
        }

        private fun drawColorPicker(setting: ColorSetting, rectMulti: PosColor2DMultiDraw, alpha: Float) {
            val area = colorPickerArea ?: return
            val hsColor = ColorUtils.hsbToRGB(setting.value.hue, 1f, 1f).alpha(alpha)
            
            // Draw background for picker rect to avoid transparency issues
            rectMulti.addRect(area.left, area.top, area.width, area.height, ColorRGB.WHITE.alpha(alpha))
            
            rectMulti.addRectGradientHorizontal(area.left, area.top, area.width, area.height, ColorRGB.WHITE.alpha(alpha), hsColor)
            rectMulti.addRectGradientVertical(area.left, area.top, area.width, area.height, ColorRGB.BLACK.alpha(0f), ColorRGB.BLACK.alpha(alpha))

            val indicatorX = area.left + setting.value.saturation * area.width
            val indicatorY = area.top + (1f - setting.value.brightness) * area.height
            
            // Draw indicator with outline
            rectMulti.addRect(indicatorX - 3f, indicatorY - 3f, 6f, 6f, ColorRGB.BLACK.alpha(alpha))
            rectMulti.addRect(indicatorX - 2f, indicatorY - 2f, 4f, 4f, ColorRGB.WHITE.alpha(alpha))
        }

        private fun drawHueBar(setting: ColorSetting, rectMulti: PosColor2DMultiDraw, alpha: Float) {
            val area = hueBarArea ?: return
            val segmentHeight = area.height / 6f
            val colors = arrayOf(
                ColorRGB(255, 0, 0), ColorRGB(255, 0, 255),
                ColorRGB(0, 0, 255), ColorRGB(0, 255, 255),
                ColorRGB(0, 255, 0), ColorRGB(255, 255, 0),
                ColorRGB(255, 0, 0)
            )

            for (i in 0 until 6) {
                rectMulti.addRectGradientVertical(
                    area.left,
                    area.top + i * segmentHeight,
                    area.width,
                    segmentHeight,
                    colors[i].alpha(alpha),
                    colors[i + 1].alpha(alpha)
                )
            }
            val indicatorY = area.top + (1f - setting.value.hue) * area.height
            rectMulti.addRect(area.left - 1f, indicatorY - 1f, area.width + 2f, 2f, ColorRGB.WHITE.alpha(alpha))
        }

        private fun drawAlphaBar(setting: ColorSetting, rectMulti: PosColor2DMultiDraw, alpha: Float) {
            val area = alphaBarArea ?: return
            
            // Draw checkerboard or dark background
            rectMulti.addRect(area.left, area.top, area.width, area.height, ColorRGB(0.2f, 0.2f, 0.2f).alpha(alpha))
            
            rectMulti.addRectGradientVertical(
                area.left, area.top, area.width, area.height,
                setting.value.alpha(1f).alpha(alpha),
                setting.value.alpha(0f).alpha(alpha)
            )
            val indicatorY = area.top + (1f - setting.value.aFloat) * area.height
            rectMulti.addRect(area.left - 1f, indicatorY - 1f, area.width + 2f, 2f, ColorRGB.WHITE.alpha(alpha))
        }

        private fun updateColorFromPicker(setting: ColorSetting, mouseX: Float, mouseY: Float) {
            val area = colorPickerArea ?: return
            val saturation = (mouseX - area.left) / area.width
            val brightness = 1f - (mouseY - area.top) / area.height
            setting.value(ColorUtils.hsbToRGB(
                setting.value.hue,
                saturation.coerceIn(0f, 1f),
                brightness.coerceIn(0f, 1f),
                setting.value.aFloat
            ))
        }

        private fun updateHueFromMouse(setting: ColorSetting, mouseY: Float) {
            val area = hueBarArea ?: return
            if (mouseY !in area.top..area.bottom) return
            val hue = 1f - (mouseY - area.top) / area.height
            setting.value(ColorUtils.hsbToRGB(
                hue.coerceIn(0f, 1f),
                setting.value.saturation,
                setting.value.brightness,
                setting.value.aFloat
            ))
        }

        private fun updateAlphaFromMouse(setting: ColorSetting, mouseY: Float) {
            val area = alphaBarArea ?: return
            if (mouseY !in area.top..area.bottom) return
            val a = 1f - (mouseY - area.top) / area.height
            setting.value(setting.value.alpha(a.coerceIn(0f, 1f)))
        }
    }

    private data class Area(val left: Float, val top: Float, val width: Float, val height: Float) {
        val right get() = left + width
        val bottom get() = top + height

        fun contains(x: Float, y: Float) = x in left..right && y in top..bottom
    }

    companion object {
        fun openScreen() {
            if (Axolotl.mc.screen?.title?.string != "Axolotl-ClickGUI") {
                DropdownClickGUI().openScreen()
            }
        }
    }
}
