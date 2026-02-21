package awa.hyw.Axolotl.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import awa.hyw.Axolotl.event.impl.ResolutionUpdateEvent;
import awa.hyw.Axolotl.graphics.RenderSystem;
import awa.hyw.Axolotl.graphics.shader.impl.MotionBlurShader;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.At;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;

@Patch(GameRenderer.class)
public class GameRendererPatch {
    private static final Logger log = LogManager.getLogger(GameRendererPatch.class);

    @Inject(method = "render", desc = "(FJZ)V",
            at = @At(value = At.Type.AFTER_INVOKE,
                    method = "net/minecraft/client/gui/Gui/render",
                    desc = "(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    public static void onRender2D(GameRenderer instance, float partialTicks, long finishTimeNano, boolean renderLevel,
                                  CallbackInfo ci) throws Exception {
        RenderSystem.INSTANCE.onRender2d(partialTicks);
    }

    @Inject(method = "resize", desc = "(II)V",
            at = @At(value = At.Type.HEAD))
    public static void onResize(GameRenderer instance, int width, int height, CallbackInfo ci) {
        new ResolutionUpdateEvent(width, height).post();
    }

    @Inject(method = "renderLevel", desc = "(FJLcom/mojang/blaze3d/vertex/PoseStack;)V",
            at = @At(value = At.Type.AFTER_INVOKE,
                    method = "net/minecraft/client/renderer/LevelRenderer/renderLevel",
                    desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;FJZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;)V"))
    public static void onRender3D(GameRenderer instance, float pPartialTicks, long pFinishTimeNano, PoseStack pPoseStack,
                                  CallbackInfo ci) {
        MotionBlurShader.INSTANCE.saveTex();
        RenderSystem.INSTANCE.onRender3d(pPartialTicks, pPoseStack);
    }
}


