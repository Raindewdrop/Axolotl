package awa.hyw.Axolotl.injection.patch;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import awa.hyw.Axolotl.injection.accessor.CameraAccessor;
import awa.hyw.Axolotl.module.impl.render.Animations;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.At;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;

@Patch(Camera.class)
public class CameraPatch {
    @Inject(method = "setup", desc = "(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V", at = @At(At.Type.TAIL))
    public static void setup(Camera camera, BlockGetter pLevel, Entity pEntity,
                             boolean pDetached, boolean pThirdPersonReverse,
                             float pPartialTick, CallbackInfo callbackInfo) {
        if (Animations.INSTANCE.getOldCamera()) {
            ((CameraAccessor) camera).move(-0.05000000074505806F, 0.0F, 0.0F);
            ((CameraAccessor) camera).move(0.1F, 0.0F, 0.0F);
            ((CameraAccessor) camera).move(-0.15F, 0, 0);
        }
    }
}


