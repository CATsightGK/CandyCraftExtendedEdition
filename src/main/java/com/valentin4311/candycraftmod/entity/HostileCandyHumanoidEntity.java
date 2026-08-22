package com.valentin4311.candycraftmod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/** Keeps hostile humanoids separate from the two legacy passive golem mounts. */
public final class HostileCandyHumanoidEntity extends BasicCandyZombieEntity implements Enemy {
    public HostileCandyHumanoidEntity(EntityType<? extends BasicCandyZombieEntity> type, Level level) {
        super(type, level);
    }
}
