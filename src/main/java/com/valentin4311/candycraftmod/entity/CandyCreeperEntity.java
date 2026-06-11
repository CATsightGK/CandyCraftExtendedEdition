package com.valentin4311.candycraftmod.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

public class CandyCreeperEntity extends Creeper {
    public CandyCreeperEntity(EntityType<? extends CandyCreeperEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return effect.getEffect() != MobEffects.POISON && super.canBeAffected(effect);
    }
}
