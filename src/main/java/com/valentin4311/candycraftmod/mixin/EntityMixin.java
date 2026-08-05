package com.valentin4311.candycraftmod.mixin;

import com.valentin4311.candycraftmod.entity.PurpleJellyStuckEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Entity.class)
public abstract class EntityMixin implements PurpleJellyStuckEntity {
    @Unique
    private boolean candycraft$purpleJellyStuck;

    @Override
    public void candycraft$setPurpleJellyStuck() {
        candycraft$purpleJellyStuck = true;
    }

    @ModifyVariable(
        method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private Vec3 candycraft$applyLegacyPurpleJellyStuckMovement(Vec3 movement) {
        if (!candycraft$purpleJellyStuck) {
            return movement;
        }

        candycraft$purpleJellyStuck = false;
        Entity entity = (Entity)(Object)this;
        entity.setDeltaMovement(Vec3.ZERO);
        return movement.multiply(0.25D, 0.05D, 0.25D);
    }
}
