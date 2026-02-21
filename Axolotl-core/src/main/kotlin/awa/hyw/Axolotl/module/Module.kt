package awa.hyw.Axolotl.module

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.glfw.GLFW
import awa.hyw.Axolotl.Axolotl
import awa.hyw.Axolotl.event.EventBus
import awa.hyw.Axolotl.event.impl.Render2DEvent
import awa.hyw.Axolotl.graphics.utils.RenderUtils2D
import awa.hyw.Axolotl.gui.notification.NotificationManager
import awa.hyw.Axolotl.gui.notification.NotificationType
import awa.hyw.Axolotl.settings.*
import awa.hyw.Axolotl.translation.TranslationString
import awa.hyw.Axolotl.util.input.KeyBind
import java.util.concurrent.CopyOnWriteArrayList

abstract class Module(
    val name: String,
    val category: Category,
    val loadFromConfig: Boolean = true,
    defaultKeybind: Int = -1,
) : SettingsDesigner<Module> {
    enum class Category(val translation: TranslationString) {
        COMBAT(TranslationString("categories", "combat", "Combat")),
        RENDER(TranslationString("categories", "render", "Render")),
        MOVEMENT(TranslationString("categories", "movement", "Movement")),
        PLAYER(TranslationString("categories", "player", "Player")),
        MISC(TranslationString("categories", "misc", "Misc"));

        fun getDisplayName(): String = translation.translation
    }

    val settings = CopyOnWriteArrayList<AbstractSetting<*>>()
    val translation = TranslationString("modules", name, name)

    private val enabled0 = BooleanSetting(TranslationString("modules", "enabled", "Enabled"), false) { false }
    var enabled by enabled0

    val hidden = BooleanSetting(TranslationString("modules", "hidden", "Hidden"), false)

    private val keyBind0 = KeyBindSetting(TranslationString("modules", "key-bind", "Keybind"), KeyBind())
    var keyBind by keyBind0

    init {
        settings.add(enabled0)
        settings.add(hidden)
        settings.add(keyBind0)

        enabled0.onChangeValue {
            if (enabled) {
                EventBus.register(this)
                onEnable()
                if (Axolotl.mc.player != null && !hidden.value) {
                    NotificationManager.show(getDisplayName(), "Enabled", NotificationType.ENABLE)
                }
            } else {
                onDisable()
                EventBus.unregister(this)
                if (Axolotl.mc.player != null && !hidden.value) {
                    NotificationManager.show(getDisplayName(), "Disabled", NotificationType.DISABLE)
                }
            }
        }

        defaultKeybind.let {
            keyBind = KeyBind(KeyBind.Type.KEYBOARD, it, GLFW.glfwGetKeyScancode(it))
        }
    }

    protected val mc by lazy { Axolotl.mc }
    protected val logger: Logger by lazy { LogManager.getLogger(javaClass) }

    fun toggle() {
        enabled = !enabled
    }

    open fun onEnable() {}
    open fun onDisable() {}
    open fun onLoad() {}

    final override fun <S : AbstractSetting<*>> Module.setting(setting: S): S {
        setting.name.key.prefix = "modules.$name"
        settings.add(setting)
        return setting
    }

    fun getDisplayName(): String {
        return translation.translation
    }
}

abstract class RenderableModule(
    name: String,
    category: Category,
    defaultX: Float,
    defaultY: Float,
    var width: Float = 0f,
    var height: Float = 0f
) : Module(name, category) {
    private val x0 = FloatSetting(
        TranslationString("modules.renderable", "x", "X"), defaultX,
        minValue = 0f, maxValue = mc.window.width.toFloat(), step = mc.window.width / 500f,
        visibility = { false }
    )
    var x by x0

    private val y0 = FloatSetting(
        TranslationString("modules.renderable", "y", "Y"), defaultY,
        minValue = 0f, maxValue = mc.window.height.toFloat(), step = mc.window.height / 500f,
        visibility = { false }
    )
    var y by y0

    val x1: Float
        get() = x + width

    val y1: Float
        get() = y + height

    init {
        settings.add(x0)
        settings.add(y0)
    }

    open fun render(event: Render2DEvent) {}

    fun isMouseOver(mouseX: Float, mouseY: Float): Boolean {
        return RenderUtils2D.isMouseOver(mouseX, mouseY, x, y, x1, y1)
    }
}
