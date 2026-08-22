package com.valentin4311.candycraftmod.mixin.client;

import com.valentin4311.candycraftmod.world.CCDimensions;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    private static final float CANDY_WORLD_MIN_SKY_BRIGHTNESS = 0.2766064F;

    @Inject(method = "getSkyDarken", at = @At("RETURN"), cancellable = true)
    private void candycraft$keepCandyWorldNightBright(float partialTick,
            CallbackInfoReturnable<Float> callback) {
        ClientLevel level = (ClientLevel)(Object)this;
        if (level.dimension() == CCDimensions.CANDY_WORLD) {
            callback.setReturnValue(Math.max(callback.getReturnValue(), CANDY_WORLD_MIN_SKY_BRIGHTNESS));
        }
    }
}
