package awa.hyw.Axolotl.gui.window

/**
 * @author LangYa466
 * @since 2/15/2025
 */
class DragWindow(var x: Float, var y: Float, var width: Float, var height: Float) {
    private var dragging: Boolean = false
    private var dragOffsetX: Float = 0f
    private var dragOffsetY: Float = 0f

    /**
     * Check if mouse is in header area
     */
    fun shouldDrag(mouseX: Float, mouseY: Float, headerHeight: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + headerHeight)
    }

    /**
     * Check if mouse is hovering window
     */
    fun isHovered(mouseX: Float, mouseY: Float): Boolean {
        return mouseX in x..(x + width) && mouseY in y..(y + height)
    }

    /**
     * Start dragging
     */
    fun startDrag(mouseX: Float, mouseY: Float) {
        dragging = true
        dragOffsetX = mouseX - x
        dragOffsetY = mouseY - y
    }

    /**
     * Update window position while dragging
     */
    fun update(mouseX: Float, mouseY: Float) {
        if (dragging) {
            x = mouseX - dragOffsetX
            y = mouseY - dragOffsetY
        }
    }

    /**
     * Stop dragging
     */
    fun stopDrag(): Boolean {
        if (dragging) {
            dragging = false
            return true
        }
        return false
    }
}

