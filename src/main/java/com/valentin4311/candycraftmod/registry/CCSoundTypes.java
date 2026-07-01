package com.valentin4311.candycraftmod.registry;

import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;

public final class CCSoundTypes {
    public static final SoundType JELLY = new DeferredSoundType(0.7F, 0.6F, () -> CCSoundEvents.DIG_JELLY.get());

    private CCSoundTypes() {
    }

    private static final class DeferredSoundType extends SoundType {
        private final Supplier<SoundEvent> sound;

        private DeferredSoundType(float volume, float pitch, Supplier<SoundEvent> sound) {
            super(volume, pitch, SoundEvents.SLIME_BLOCK_BREAK, SoundEvents.SLIME_BLOCK_STEP,
                SoundEvents.SLIME_BLOCK_PLACE, SoundEvents.SLIME_BLOCK_HIT, SoundEvents.SLIME_BLOCK_FALL);
            this.sound = sound;
        }

        @Override
        public SoundEvent getBreakSound() {
            return sound.get();
        }

        @Override
        public SoundEvent getStepSound() {
            return sound.get();
        }

        @Override
        public SoundEvent getPlaceSound() {
            return sound.get();
        }

        @Override
        public SoundEvent getHitSound() {
            return sound.get();
        }

        @Override
        public SoundEvent getFallSound() {
            return sound.get();
        }
    }
}
