package awa.hyw.Axolotl.injection.patch;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import awa.hyw.Axolotl.module.impl.render.Particles;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;

@Patch(ClientLevel.class)
public class ClientLevelPatch {
    @Inject(method = "addDestroyBlockEffect", desc = "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V")
    public static void addDestroyBlockEffect(ClientLevel level, BlockPos pos, BlockState state, CallbackInfo ci) {
        ci.cancelled = Particles.INSTANCE.getEnabled() && Particles.INSTANCE.getBlockBreaking();
    }
}


