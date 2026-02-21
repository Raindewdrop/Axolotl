package awa.hyw.Axolotl.injection.patch;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import awa.hyw.Axolotl.Axolotl;
import awa.hyw.Axolotl.event.impl.PostTickEvent;
import awa.hyw.Axolotl.event.impl.PreTickEvent;
import awa.hyw.Axolotl.event.impl.ShutdownEvent;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.At;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;
import awa.hyw.common.status.Status;

@Patch(Minecraft.class)
public class MinecraftPatch {
    private static final Logger log = LogManager.getLogger(MinecraftPatch.class);
    private static volatile boolean initialized = false;

    @Inject(method = "tick", desc = "()V")
    public static void onTickPre(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        if (!initialized) {
            synchronized (MinecraftPatch.class) {
                if (!initialized) {
                    try {
                        log.info("Minecraft tick detected, starting Axolotl initialization...");
                        Axolotl.INSTANCE.setMc(Minecraft.getInstance());
                        Axolotl.INSTANCE.init();
                        initialized = true;
                        log.info("Axolotl initialization completed in tick loop");
                    } catch (Throwable t) {
                        log.error("Failed to initialize Axolotl in tick loop", t);
                        Axolotl.INSTANCE.getStatusReporter().invoke(Status.ERROR_GENERAL);
                    }
                }
            }
        }

        new PreTickEvent().post();
    }
    
    @Inject(method = "runTick", desc = "(Z)V")
    public static void runTick(Minecraft minecraft, boolean renderLevel, CallbackInfo ci) {
        if (!initialized) {
            synchronized (MinecraftPatch.class) {
                if (!initialized) {
                    try {
                        log.info("Minecraft runTick detected, starting Axolotl initialization...");
                        Axolotl.INSTANCE.setMc(Minecraft.getInstance());
                        Axolotl.INSTANCE.init();
                        initialized = true;
                        log.info("Axolotl initialization completed in runTick loop");
                    } catch (Throwable t) {
                        log.error("Failed to initialize Axolotl in runTick loop", t);
                        Axolotl.INSTANCE.getStatusReporter().invoke(Status.ERROR_GENERAL);
                    }
                }
            }
        }
    }

    @Inject(method = "tick", desc = "()V", at = @At(At.Type.TAIL))
    public static void onTickPost(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        new PostTickEvent().post();
    }

    @Inject(method = "destroy",desc = "()V")
    public static void destroy(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        new ShutdownEvent().post();
    }
}


