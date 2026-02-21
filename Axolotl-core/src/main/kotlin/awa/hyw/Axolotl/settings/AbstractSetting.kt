package awa.hyw.Axolotl.settings

import com.google.gson.JsonElement
import awa.hyw.Axolotl.gui.GuiScreen
import awa.hyw.Axolotl.gui.widget.AbstractSettingWidget
import awa.hyw.Axolotl.translation.TranslationString
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class AbstractSetting<T : Any>(
    var name: TranslationString,
    var value: T,
    var visibility: () -> Boolean
) : ReadWriteProperty<Any, T> {
    private val defaultValue = value

    private val changeValueConsumers = CopyOnWriteArrayList<(setting: AbstractSetting<T>) -> Unit>()

    val settingId: String
        get() = "$name@${this::class.simpleName}"

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        value(value)
    }

    fun onChangeValue(run: (setting: AbstractSetting<T>) -> Unit): AbstractSetting<T> {
        return this.apply { changeValueConsumers.add(run) }
    }

    fun default() {
        value = defaultValue
    }

    //Builder
    fun key(key: TranslationString): AbstractSetting<T> {
        this.name = key
        return this
    }

    fun value(value: T): AbstractSetting<T> {
        this.value = value
        changeValueConsumers.forEach { it.invoke(this) }
        return this
    }

    fun visibility(visibility: () -> Boolean): AbstractSetting<T> {
        this.visibility = visibility
        return this
    }

    abstract fun toJson(): JsonElement
    abstract fun fromJson(json: JsonElement)

    abstract fun createWidget(screen: GuiScreen): AbstractSettingWidget
}

