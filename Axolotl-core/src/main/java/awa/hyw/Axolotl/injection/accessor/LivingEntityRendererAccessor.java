package awa.hyw.Axolotl.injection.accessor;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import awa.hyw.patchify.annotation.Accessor;
import awa.hyw.patchify.annotation.MethodAccessor;

@Accessor(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
    @MethodAccessor
    float getWhiteOverlayProgress(LivingEntity pLivingEntity, float pPartialTicks);
}


