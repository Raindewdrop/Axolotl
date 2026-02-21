package awa.hyw.Axolotl

import net.minecraft.client.Minecraft
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.objectweb.asm.Type
import awa.hyw.common.status.Status
import awa.hyw.Axolotl.config.ConfigManager
import awa.hyw.Axolotl.event.EventBus
import awa.hyw.Axolotl.event.EventTarget
import awa.hyw.Axolotl.event.impl.PostInitEvent
import awa.hyw.Axolotl.event.impl.PostTickEvent
import awa.hyw.Axolotl.event.impl.PreInitEvent
import awa.hyw.Axolotl.event.impl.ShutdownEvent
import awa.hyw.Axolotl.graphics.RenderSystem
import awa.hyw.Axolotl.graphics.buffer.VertexBufferObjects
import awa.hyw.Axolotl.graphics.font.FontRenderers
import awa.hyw.Axolotl.graphics.utils.RenderUtils
import awa.hyw.Axolotl.gui.clickgui.DropdownClickGUI
import awa.hyw.Axolotl.gui.notification.Notification
import awa.hyw.Axolotl.gui.notification.NotificationManager
import awa.hyw.Axolotl.gui.notification.NotificationType
import awa.hyw.Axolotl.injection.patch.MinecraftPatch
import awa.hyw.Axolotl.misc.ClassUtil
import awa.hyw.Axolotl.module.ModuleManager
import awa.hyw.Axolotl.util.SoundUtil
import awa.hyw.patchify.ASMUtil
import awa.hyw.patchify.PatchLoader
import awa.hyw.patchify.annotation.Patch
import awa.hyw.patchify.asm.ReflectionUtil
import kotlin.concurrent.thread
import kotlin.properties.Delegates
import com.mojang.blaze3d.systems.RenderSystem as MojangRenderSystem

object Axolotl {
    const val NAME = "Axolotl"
    const val VERSION = "1.0.0"
    // Red (0xFF0000FF) to Pink (0xFF00FFFF)
    const val GRADIENT_START = -16776961
    const val GRADIENT_END = -16711681
    lateinit var classes: MutableMap<String, ByteArray>
    lateinit var mc: Minecraft
    lateinit var statusReporter: (Status) -> Unit
    val log: Logger = LogManager.getLogger(javaClass)
    var obfuscated by Delegates.notNull<Boolean>()

    const val ASSETS_DIRECTORY = "/assets/Axolotl"
    
    private var firstJoin = true

    fun init() {
        log.info("Start initializing Axolotl...")
        PreInitEvent().post()

        statusReporter.invoke(Status.CORE_PATCH)
        classes
            .filter { ASMUtil.isVisibleAnnotationPresent(ASMUtil.node(it.value), Patch::class.java) }
            .filter { it.key != Type.getInternalName(MinecraftPatch::class.java) }
            .map { ReflectionUtil.forName(it.key) }
            .let {
                it.forEach { patch ->
                    PatchLoader.INSTANCE.loadPatch(
                        patch,
                        { clazz -> ClassUtil.getClassBytes(clazz) },
                        { clazz, bytes: ByteArray? -> ClassUtil.redefineClass(clazz!!, bytes!!) }
                    )
                }
                log.info("Loaded {} patch, total classes: {}", it.size, classes.size)
            }

        ModuleManager.init()
        ConfigManager.init()
        
        // Register NotificationManager
        EventBus.register(NotificationManager)

        MojangRenderSystem.recordRenderCall {
            // GUI
            DropdownClickGUI
            // Systems
            VertexBufferObjects
            RenderSystem
            // Fonts
            FontRenderers
            // Utils
            RenderUtils
        }

        PostInitEvent().post()
        statusReporter.invoke(Status.SUCCESS)
        log.info("Axolotl has been successfully initialized!")

        SoundUtil.playSound("success")
    }

    @EventTarget
    fun onTick(event: PostTickEvent) {
        if (firstJoin && !isNull()) {
            firstJoin = false
            thread {
                Thread.sleep(1000)
                NotificationManager.show(Notification("Axolotl", "Injection Successful!", NotificationType.SUCCESS))
            }
        }
    }

    @EventTarget
    fun onShutdown(event: ShutdownEvent) {
        ConfigManager.shutdown()
        EventBus.unregister(NotificationManager)
    }

    fun isNull(): Boolean {
        return mc.player == null
    }
}
