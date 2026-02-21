package awa.hyw.Axolotl.module.impl.render

import awa.hyw.Axolotl.graphics.color.ColorRGB
import awa.hyw.Axolotl.module.Module

object Hitboxes: Module("Hitboxes", Category.RENDER) {
    val color by setting("color", ColorRGB.Companion.WHITE)
    val showViewVector by setting("show-eye-vector", true)
    val viewVectorColor by setting("eye-vector-color", ColorRGB(0, 0, 255, 254)) { showViewVector }

    val players by setting("players", true)
    val items by setting("items", true)
    val mobs by setting("mobs", true)
    val projectiles by setting("projectiles", true)

    override fun onEnable() {
        if (!mc.entityRenderDispatcher.shouldRenderHitBoxes()) {
            mc.entityRenderDispatcher.setRenderHitBoxes(true)
        }
    }

    override fun onDisable() {
        if (mc.entityRenderDispatcher.shouldRenderHitBoxes()) {
            mc.entityRenderDispatcher.setRenderHitBoxes(false)
        }
    }
}