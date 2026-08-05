package com.valentin4311.candycraftmod.mixin;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PressurePlateBlock.class)
public abstract class PressurePlateBlockMixin {
    private static final AABB PRESSURE_PLATE_TOUCH_AREA = new AABB(
        0.0625D, 0.0D, 0.0625D,
        0.9375D, 0.25D, 0.9375D
    );

    @Inject(method = "getSignalStrength", at = @At("HEAD"), cancellable = true)
    private void candycraft$excludeMintJelly(Level level, BlockPos pos, CallbackInfoReturnable<Integer> callback) {
        if ((Object)this != Blocks.STONE_PRESSURE_PLATE) {
            return;
        }

        boolean hasTriggeringEntity = level.getEntitiesOfClass(
            LivingEntity.class,
            PRESSURE_PLATE_TOUCH_AREA.move(pos)
        ).stream().anyMatch(entity ->
            !entity.isIgnoringBlockTriggers() && entity.getType() != CCEntityTypes.TORNADO_JELLY.get()
        );
        callback.setReturnValue(hasTriggeringEntity ? 15 : 0);
    }
}
