package awa.hyw.Axolotl.injection.patch;

import awa.hyw.Axolotl.Axolotl;
import awa.hyw.Axolotl.config.impl.SkinManager;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

@Patch(AbstractClientPlayer.class)
public class AbstractClientPlayerPatch {

    @Inject(method = "m_108560_", desc = "()Lnet/minecraft/resources/ResourceLocation;")
    public static void getSkinTextureLocation(AbstractClientPlayer instance, CallbackInfo ci) {
        if (instance == Axolotl.mc.player) {
            ResourceLocation customSkin = SkinManager.INSTANCE.getCustomSkinLocation();
            if (customSkin != null) {
                ci.result = customSkin;
                ci.cancelled = true;
            }
        }
    }

    @Inject(method = "m_108561_", desc = "()Lnet/minecraft/resources/ResourceLocation;")
    public static void getCloakTextureLocation(AbstractClientPlayer instance, CallbackInfo ci) {
        if (instance == Axolotl.mc.player) {
            ResourceLocation customCape = SkinManager.INSTANCE.getCustomCapeLocation();
            if (customCape != null) {
                ci.result = customCape;
                ci.cancelled = true;
            }
        }
    }

    @Inject(method = "m_108564_", desc = "()Ljava/lang/String;")
    public static void getModelName(AbstractClientPlayer instance, CallbackInfo ci) {
        if (instance == Axolotl.mc.player) {
             ResourceLocation customSkin = SkinManager.INSTANCE.getCustomSkinLocation();
             if (customSkin != null) {
                 ci.result = SkinManager.INSTANCE.getSkinType();
                 ci.cancelled = true;
             }
        }
    }
}
