package awa.hyw.Axolotl.injection.accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import awa.hyw.patchify.annotation.Accessor;
import awa.hyw.patchify.annotation.FieldAccessor;

@Accessor(LevelRenderer.class)
public interface LevelRendererAccessor {
    @FieldAccessor("level")
    ClientLevel level();
}


