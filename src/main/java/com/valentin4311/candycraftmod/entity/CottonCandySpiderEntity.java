package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCMobEffects;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class CottonCandySpiderEntity extends Spider {
    public CottonCandySpiderEntity(EntityType<? extends CottonCandySpiderEntity> type, Level level) {
        super(type, level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        // CandyCraft 1.8.9 deliberately bypassed Spider.finalizeSpawn, preventing spider jockeys and random effects.
        return spawnData;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!CandyTargeting.canAttackEntity(target)) {
            setTarget(null);
            return false;
        }
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                int seconds = level().getDifficulty() == Difficulty.HARD ? 15 : 7;
                living.addEffect(new MobEffectInstance(CCMobEffects.CLOYING.get(), seconds * 20), this);
            }
            return true;
        }
        return false;
    }

    @Override
    public float getVoicePitch() {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.6F;
    }
}
