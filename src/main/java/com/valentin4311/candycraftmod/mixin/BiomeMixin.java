package com.valentin4311.candycraftmod.mixin;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeMixin {
    @Inject(method = "shouldFreeze(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Z",
        at = @At("HEAD"), cancellable = true)
    private void candycraft$preventCandyWorldFreezing(LevelReader level, BlockPos pos, boolean mustBeAtEdge,
            CallbackInfoReturnable<Boolean> callback) {
        if (isCandyWorld(level)) {
            callback.setReturnValue(false);
        }
    }

    @Inject(method = "shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z",
        at = @At("HEAD"), cancellable = true)
    private void candycraft$preventCandyWorldSnowLayers(LevelReader level, BlockPos pos,
            CallbackInfoReturnable<Boolean> callback) {
        if (isCandyWorld(level)) {
            callback.setReturnValue(false);
        }
    }

    private static boolean isCandyWorld(LevelReader level) {
        if (!(level instanceof Level actualLevel)) {
            return false;
        }
        ResourceLocation dimension = actualLevel.dimension().location();
        return CandyCraft.MODID.equals(dimension.getNamespace()) && "candy_world".equals(dimension.getPath());
    }
}
