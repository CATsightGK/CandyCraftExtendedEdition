package com.valentin4311.candycraftmod.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

public class CottonCandySpiderEntity extends Spider {
    public CottonCandySpiderEntity(EntityType<? extends CottonCandySpiderEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                int seconds = level().getDifficulty() == Difficulty.HARD ? 15 : level().getDifficulty() == Difficulty.NORMAL ? 7 : 0;
                if (seconds > 0) {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, seconds * 20), this);
                }
            }
            return true;
        }
        return false;
    }
}
