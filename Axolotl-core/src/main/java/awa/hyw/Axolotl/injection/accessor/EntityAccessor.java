package awa.hyw.Axolotl.injection.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import awa.hyw.patchify.annotation.Accessor;
import awa.hyw.patchify.annotation.FieldAccessor;
import awa.hyw.patchify.annotation.MethodAccessor;

@Accessor(Entity.class)
public interface EntityAccessor {
    @FieldAccessor("stuckSpeedMultiplier")
    Vec3 getStuckSpeedMultiplier();

    @MethodAccessor
    boolean canEnterPose(Pose pPose);
}


