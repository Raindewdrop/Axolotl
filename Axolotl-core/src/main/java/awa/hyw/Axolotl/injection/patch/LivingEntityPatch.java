package awa.hyw.Axolotl.injection.patch;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import awa.hyw.Axolotl.module.impl.render.Animations;
import awa.hyw.Axolotl.module.impl.render.Particles;
import awa.hyw.patchify.annotation.Patch;
import awa.hyw.patchify.annotation.WrapInvoke;
import awa.hyw.patchify.api.Invocation;

@Patch(LivingEntity.class)
public class LivingEntityPatch {
    private static final Minecraft mc = Minecraft.getInstance();

    @WrapInvoke(method = "tickEffects", desc = "()V",
            target = "net/minecraft/world/level/Level/addParticle",
            targetDesc = "(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V")
    public static void wrapTickEffects(LivingEntity entity, Invocation<Level, Void> invocation) throws Exception {
        if (entity == mc.player && mc.options.getCameraType().isFirstPerson() &&
                Particles.INSTANCE.getEnabled() && Particles.INSTANCE.getShowFirstPerson()) {
            return;
        }
        invocation.call();
    }

    @WrapInvoke(method = "tick", desc = "()V", target = "net/minecraft/util/Mth/abs", targetDesc = "(F)F")
    public static float animatium$rotateBackwardsWalking(LivingEntity entity, Invocation<Void, Float> original) throws Exception {
        if (Animations.INSTANCE.getRoteteBackwards()) {
            return 0F;
        } else {
            return original.call();
        }
    }
}


