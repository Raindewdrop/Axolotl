package awa.hyw.Axolotl.config.impl

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import awa.hyw.Axolotl.Axolotl.mc
import awa.hyw.Axolotl.config.Config
import awa.hyw.Axolotl.util.ChatUtil
import net.minecraft.client.renderer.texture.HttpTexture
import net.minecraft.resources.ResourceLocation
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture

object SkinManager : Config("Skin") {
    var skinName: String = ""
    var slim: Boolean = true
    
    var customSkinLocation: ResourceLocation? = null
    var customCapeLocation: ResourceLocation? = null
    var skinType: String = "default" // "default" or "slim"

    override fun saveConfig(): JsonObject {
        val json = JsonObject()
        json.addProperty("skinName", skinName)
        json.addProperty("slim", slim)
        return json
    }

    override fun loadConfig(jsonObject: JsonObject) {
        if (jsonObject.has("skinName")) {
            skinName = jsonObject.get("skinName").asString
        }
        if (jsonObject.has("slim")) {
            slim = jsonObject.get("slim").asBoolean
        }
        
        if (skinName.isNotEmpty()) {
            loadSkin(skinName)
        }
    }
    
    fun loadSkin(name: String) {
        skinName = name
        ChatUtil.addMessageWithClient("Starting skin load for $name")
        CompletableFuture.runAsync {
            try {
                // 1. Get UUID
                val uuidUrl = URL("https://api.mojang.com/users/profiles/minecraft/$name")
                val uuidConnection = uuidUrl.openConnection() as HttpURLConnection
                uuidConnection.connectTimeout = 5000
                uuidConnection.readTimeout = 5000
                if (uuidConnection.responseCode != 200) {
                    ChatUtil.addMessageWithClient("Failed to get UUID for $name")
                    return@runAsync
                }
                
                val uuidJson = JsonParser.parseString(uuidConnection.inputStream.reader().readText()).asJsonObject
                val uuid = uuidJson.get("id").asString
                
                // 2. Get Profile
                val profileUrl = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
                val profileConnection = profileUrl.openConnection() as HttpURLConnection
                profileConnection.connectTimeout = 5000
                profileConnection.readTimeout = 5000
                if (profileConnection.responseCode != 200) {
                    ChatUtil.addMessageWithClient("Failed to get profile for $name")
                    return@runAsync
                }
                
                val profileJson = JsonParser.parseString(profileConnection.inputStream.reader().readText()).asJsonObject
                val properties = profileJson.getAsJsonArray("properties")
                var textureData = ""
                
                for (element in properties) {
                    val property = element.asJsonObject
                    if (property.get("name").asString == "textures") {
                        textureData = property.get("value").asString
                        break
                    }
                }
                
                if (textureData.isEmpty()) {
                    ChatUtil.addMessageWithClient("No texture data found for $name")
                    return@runAsync
                }
                
                // 3. Decode Texture Data
                val decoded = String(Base64.getDecoder().decode(textureData))
                val textureJson = JsonParser.parseString(decoded).asJsonObject
                val textures = textureJson.getAsJsonObject("textures")
                
                if (textures.has("SKIN")) {
                    val skinObj = textures.getAsJsonObject("SKIN")
                    val skinUrl = skinObj.get("url").asString
                    
                    // Check model type
                    var isSlim = false
                    if (skinObj.has("metadata")) {
                        val metadata = skinObj.getAsJsonObject("metadata")
                        if (metadata.has("model")) {
                            val model = metadata.get("model").asString
                            if (model == "slim") {
                                isSlim = true
                            }
                        }
                    }
                    slim = isSlim

                    // Download and Register Skin
                    mc.execute {
                        val location = ResourceLocation("axolotl", "skins/$uuid")
                        val file = File(mc.gameDirectory, "assets/skins/$uuid.png")
                        file.parentFile.mkdirs()
                        
                        val texture = HttpTexture(
                            file,
                            skinUrl,
                            Constants.DEFAULT_SKIN_LOCATION, // Fallback
                            true,
                            Runnable { ChatUtil.addMessageWithClient("Skin texture downloaded for $name") }
                        )
                        
                        mc.textureManager.register(location, texture)
                        customSkinLocation = location
                        skinType = if (slim) "slim" else "default"
                        ChatUtil.addMessageWithClient("Loaded skin for $name")
                    }
                } else {
                     ChatUtil.addMessageWithClient("No skin found for $name")
                }

                if (textures.has("CAPE")) {
                    val capeObj = textures.getAsJsonObject("CAPE")
                    val capeUrl = capeObj.get("url").asString

                    // Download and Register Cape
                    mc.execute {
                        val location = ResourceLocation("axolotl", "capes/$uuid")
                        val file = File(mc.gameDirectory, "assets/capes/$uuid.png")
                        file.parentFile.mkdirs()
                        
                        val texture = HttpTexture(
                            file,
                            capeUrl,
                            Constants.DEFAULT_CAPE_LOCATION, // Fallback
                            false,
                            Runnable { ChatUtil.addMessageWithClient("Cape texture downloaded for $name") }
                        )
                        
                        mc.textureManager.register(location, texture)
                        customCapeLocation = location
                        ChatUtil.addMessageWithClient("Loaded cape for $name")
                    }
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                ChatUtil.addMessageWithClient("Error loading skin: ${e.message}")
            }
        }
    }
    
    // Helper object to hold constants if needed, or just import
    object Constants {
        val DEFAULT_SKIN_LOCATION = ResourceLocation("textures/entity/steve.png")
        val DEFAULT_CAPE_LOCATION = ResourceLocation("textures/entity/cape_missing.png") // Or some valid fallback
    }
}
