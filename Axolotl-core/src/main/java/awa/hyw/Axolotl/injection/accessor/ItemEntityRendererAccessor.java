package awa.hyw.Axolotl.injection.accessor;

import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import awa.hyw.patchify.annotation.Accessor;
import awa.hyw.patchify.annotation.FieldAccessor;

@Accessor(ItemEntityRenderer.class)
public interface ItemEntityRendererAccessor {
    @FieldAccessor("itemRenderer")
    ItemRenderer getItemRenderer();
}


