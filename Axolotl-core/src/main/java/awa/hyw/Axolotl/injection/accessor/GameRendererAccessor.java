package awa.hyw.Axolotl.injection.accessor;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import awa.hyw.patchify.annotation.Accessor;
import awa.hyw.patchify.annotation.FieldAccessor;
import awa.hyw.patchify.annotation.Final;

@Accessor(GameRenderer.class)
public interface GameRendererAccessor {
    @Final
    @FieldAccessor(value = "overlayTexture", getter = false)
    void setOverlayTexture(OverlayTexture overlayTexture);
}


