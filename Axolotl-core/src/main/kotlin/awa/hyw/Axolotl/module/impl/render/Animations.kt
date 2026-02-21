package awa.hyw.Axolotl.module.impl.render

import net.minecraft.world.item.SwordItem
import net.minecraft.world.level.block.*
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import awa.hyw.Axolotl.module.Module

object Animations : Module(
    name = "Animations",
    category = Category.RENDER
 ) {
    val swordBlocking by setting("sword-blocking", false)
    val swing by setting("swing", false)
    val oldCamera by setting("old-camera", false)
    val roteteBackwards by setting("rotate-backwards", false)

    private val consumables = listOf(
        ChestBlock::class.java,
        EnderChestBlock::class.java,
        ShulkerBoxBlock::class.java,
        FurnaceBlock::class.java,
        CraftingTableBlock::class.java,
        SmokerBlock::class.java,
        BlastFurnaceBlock::class.java,
        CartographyTableBlock::class.java,
        AnvilBlock::class.java,
        BellBlock::class.java,
        BeaconBlock::class.java,
        DragonEggBlock::class.java,
        LeverBlock::class.java,
        ButtonBlock::class.java,
        GrindstoneBlock::class.java,
        LoomBlock::class.java,
        NoteBlock::class.java,
        FenceGateBlock::class.java,
        DoorBlock::class.java,
        StonecutterBlock::class.java,
        SignBlock::class.java,
        WallSignBlock::class.java,
        WallHangingSignBlock::class.java,
        RepeaterBlock::class.java,
        ComparatorBlock::class.java,
        DispenserBlock::class.java,
        JigsawBlock::class.java,
        CommandBlock::class.java,
        StructureBlock::class.java,
        HopperBlock::class.java,
        BedBlock::class.java,
        BarrelBlock::class.java,
        CakeBlock::class.java,
        CandleCakeBlock::class.java,
        BrewingStandBlock::class.java,
        DaylightDetectorBlock::class.java
    )

    fun shouldBlock(): Boolean {
        if (!enabled || !swordBlocking || mc.player == null || mc.level == null) return false

        val hitResult = mc.player!!.pick(5.0, 0.0f, false)
        if (hitResult.type == HitResult.Type.BLOCK) {
            val block = mc.level!!.getBlockState((hitResult as BlockHitResult).blockPos).block
            if (consumables.any { it.isInstance(block) }) return false
        }

        return mc.player!!.mainHandItem.item is SwordItem && mc.options.keyUse.isDown
    }
}

