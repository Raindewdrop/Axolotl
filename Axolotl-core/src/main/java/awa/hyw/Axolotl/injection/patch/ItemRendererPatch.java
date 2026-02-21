package awa.hyw.Axolotl.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import awa.hyw.Axolotl.event.impl.ItemRenderEvent;
import awa.hyw.patchify.CallbackInfo;
import awa.hyw.patchify.annotation.At;
import awa.hyw.patchify.annotation.Inject;
import awa.hyw.patchify.annotation.Patch;

@Patch(ItemRenderer.class)
public class ItemRendererPatch {
    @Inject(method = "renderStatic", desc = "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V")
    public static void renderStatic(ItemRenderer itemRenderer, LivingEntity livingEntity,
                                    ItemStack itemStack, ItemDisplayContext itemDisplayContext,
                                    boolean bl, PoseStack poseStack,
                                    MultiBufferSource multiBufferSource, Level level,
                                    int i, int j, int k, CallbackInfo ci) {
        new ItemRenderEvent(true, livingEntity, itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, level).post();
    }

    @Inject(method = "renderStatic", desc = "(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At(At.Type.TAIL))
    public static void renderStaticPost(ItemRenderer itemRenderer, LivingEntity livingEntity,
                                        ItemStack itemStack, ItemDisplayContext itemDisplayContext,
                                        boolean bl, PoseStack poseStack,
                                        MultiBufferSource multiBufferSource, Level level,
                                        int i, int j, int k, CallbackInfo ci) {
        new ItemRenderEvent(false, livingEntity, itemStack, itemDisplayContext, bl, poseStack, multiBufferSource, level).post();
    }
}


