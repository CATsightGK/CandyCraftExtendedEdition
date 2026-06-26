package com.valentin4311.candycraftmod.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public final class CandyTargeting {
    private CandyTargeting() {
    }

    public static boolean canAttackPlayer(Player player) {
        return player != null && !player.getAbilities().instabuild && !player.isSpectator();
    }

    public static boolean canAttackEntity(Entity entity) {
        return !(entity instanceof Player player) || canAttackPlayer(player);
    }

    public static Player nearestAttackablePlayer(Level level, Entity seeker, double range) {
        return level.getEntitiesOfClass(Player.class, seeker.getBoundingBox().inflate(range), CandyTargeting::canAttackPlayer)
            .stream()
            .min(Comparator.comparingDouble(seeker::distanceToSqr))
            .orElse(null);
    }

    public static Player nearestVisiblePlayer(Level level, Entity seeker, double range) {
        return level.getEntitiesOfClass(Player.class, seeker.getBoundingBox().inflate(range), player -> !player.isSpectator())
            .stream()
            .min(Comparator.comparingDouble(seeker::distanceToSqr))
            .orElse(null);
    }
}
