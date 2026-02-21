package awa.hyw.Axolotl.injection.accessor;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import awa.hyw.patchify.annotation.Accessor;
import awa.hyw.patchify.annotation.FieldAccessor;

@Accessor(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
    @FieldAccessor("isDestroying")
    boolean getIsDestroying();
}

