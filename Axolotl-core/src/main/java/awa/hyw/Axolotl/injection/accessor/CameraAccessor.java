package awa.hyw.Axolotl.injection.accessor;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import awa.hyw.patchify.annotation.Accessor;
import awa.hyw.patchify.annotation.FieldAccessor;
import awa.hyw.patchify.annotation.MethodAccessor;

@Accessor(Camera.class)
public interface CameraAccessor {
    @FieldAccessor("eyeHeight")
    float eyeHeight();

    @FieldAccessor(value = "eyeHeight", getter = false)
    void setEyeHeight(float height);

    @FieldAccessor(value = "eyeHeightOld", getter = false)
    void setEyeHeightOld(float height);

    @MethodAccessor
    void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset);

    @FieldAccessor("entity")
    Entity entity();
}


