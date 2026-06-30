package com.valentin4311.candycraftmod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class CandyCreeperEntity extends Creeper {
    private static final String LOLLIPOP_STALL_TAG = "LollipopStall";
    private int lollipopStallTicks;

    public CandyCreeperEntity(EntityType<? extends CandyCreeperEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void aiStep() {
        if (lollipopStallTicks > 0) {
            lollipopStallTicks--;
            setSwellDir(-1);
        }
        super.aiStep();
    }

    @Override
    public void ignite() {
        if (lollipopStallTicks <= 0) {
            super.ignite();
        }
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }

    public void stallWithLollipop(int ticks) {
        lollipopStallTicks = Math.max(lollipopStallTicks, ticks);
        setSwellDir(-1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(LOLLIPOP_STALL_TAG, lollipopStallTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        lollipopStallTicks = tag.getInt(LOLLIPOP_STALL_TAG);
    }
}
