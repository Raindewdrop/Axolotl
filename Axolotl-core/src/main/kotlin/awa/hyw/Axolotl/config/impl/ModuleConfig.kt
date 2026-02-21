package awa.hyw.Axolotl.config.impl

import com.google.gson.JsonObject
import awa.hyw.Axolotl.config.Config
import awa.hyw.Axolotl.module.ModuleManager
import awa.hyw.Axolotl.module.RenderableModule

class ModuleConfig : Config("Module") {
    override fun saveConfig(): JsonObject {
        return JsonObject().apply {
            ModuleManager.modules().forEach { module ->
                add(module.name, JsonObject().apply {
                    module.settings.forEach {
                        add(it.name.key.key, it.toJson())
                    }
                })
            }
        }
    }

    override fun loadConfig(jsonObject: JsonObject) {
        ModuleManager.modules()
            .filter { it.loadFromConfig }
            .forEach { module ->
                jsonObject.getAsJsonObject(module.name)?.let { moduleObject ->
                    module.settings.forEach { setting ->
                        runCatching {
                            setting.fromJson(moduleObject.get(setting.name.key.key))
                        }.onFailure {
                            log.warn("Failed to load setting ${setting.name.translation} for module ${module.name}")
                        }
                    }
                }
            }
    }
}

