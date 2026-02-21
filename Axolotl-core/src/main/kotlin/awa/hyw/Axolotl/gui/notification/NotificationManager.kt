package awa.hyw.Axolotl.gui.notification

import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.Render2DEvent
import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.graphics.multidraw.FontMultiDraw
import awa.hyw.Axolotl.graphics.multidraw.PosColor2DMultiDraw
import awa.hyw.Axolotl.util.SoundUtil
import java.util.concurrent.CopyOnWriteArrayList

object NotificationManager {
    private val notifications = CopyOnWriteArrayList<Notification>()
    private val fontMulti = FontMultiDraw()
    private val rectMulti = PosColor2DMultiDraw()

    fun show(title: String, message: String, type: NotificationType) {
        val notification = Notification(title, message, type)
        notifications.add(notification)
        
        when (type) {
            NotificationType.ENABLE -> SoundUtil.playSound("enable")
            NotificationType.DISABLE -> SoundUtil.playSound("disable")
            NotificationType.SUCCESS -> SoundUtil.playSound("success")
            NotificationType.WARNING, NotificationType.ERROR -> SoundUtil.playSound("warn")
            NotificationType.INFO -> SoundUtil.playSound("info")
        }
    }

    fun show(notification: Notification) {
        notifications.add(notification)
        // Sound logic duplicated if show(Notification) is called directly, 
        // but mostly we use the helper above.
    }

    @EventTarget
    fun render(event: Render2DEvent) {
        if (notifications.isEmpty()) return

        val width = Axolotl.mc.window.guiScaledWidth.toFloat()
        val height = Axolotl.mc.window.guiScaledHeight.toFloat()
        var yOffset = height - 40f

        notifications.forEach { notification ->
            notification.update()
            
            val animX = notification.animationX
            
            if (notification.isExpired && animX <= 0.05f) {
                notifications.remove(notification)
                return@forEach
            }
            
            val notifWidth = 140f
            val notifHeight = 25f
            
            // Slide in from right
            val x = width - (notifWidth * animX)
            
            // Draw logic
            rectMulti.addRect(x, yOffset, notifWidth, notifHeight, ColorRGB(0, 0, 0, 180))
            rectMulti.addRect(x, yOffset, 2f, notifHeight, notification.type.color)
            
            fontMulti.addText(notification.title, x + 8f, yOffset + 3f, ColorRGB.WHITE, false, 0.9f)
            fontMulti.addText(notification.message, x + 8f, yOffset + 13f, ColorRGB.GRAY, false, 0.7f)
            
            yOffset -= (notifHeight + 5f) * animX
        }
        
        rectMulti.draw()
        fontMulti.draw()
    }
}

data class Notification(
    val title: String,
    val message: String,
    val type: NotificationType,
    val durationMs: Long = 2500L
) {
    private val createdAt = System.currentTimeMillis()
    var animationX = 0f

    val isExpired: Boolean
        get() = System.currentTimeMillis() - createdAt >= durationMs
    
    fun update() {
        val target = if (isExpired) 0f else 1f
        animationX += (target - animationX) * 0.1f
    }
}

enum class NotificationType(val color: ColorRGB) {
    INFO(ColorRGB.WHITE),
    WARNING(ColorRGB(255, 255, 0)),
    ERROR(ColorRGB(255, 0, 0)),
    SUCCESS(ColorRGB(0, 255, 0)),
    ENABLE(ColorRGB(0, 255, 0)),
    DISABLE(ColorRGB(255, 0, 0))
}
