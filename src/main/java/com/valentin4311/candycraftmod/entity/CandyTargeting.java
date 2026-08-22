package com.valentin4311.candycraftmod.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

public final class CandyTargeting {
    private CandyTargeting() {
    }

    public static boolean canAttackPlayer(Player player) {
        return player != null && player.isAlive() && !player.getAbilities().instabuild && !player.isSpectator();
    }

    public static boolean canAttackEntity(Entity entity) {
        return entity != null && entity.isAlive()
            && (!(entity instanceof Player player) || canAttackPlayer(player));
    }

    public static Player nearestAttackablePlayer(Level level, Entity seeker, double range) {
        return nearestPlayer(level, seeker, range, CandyTargeting::canAttackPlayer);
    }

    public static Player nearestVisiblePlayer(Level level, Entity seeker, double range) {
        return nearestPlayer(level, seeker, range, player -> !player.isSpectator());
    }

    private static Player nearestPlayer(Level level, Entity seeker, double range, Predicate<Player> filter) {
        AABB searchBounds = seeker.getBoundingBox().inflate(range);
        Player nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Player player : level.players()) {
            if (!filter.test(player) || !searchBounds.intersects(player.getBoundingBox())) {
                continue;
            }
            double distance = seeker.distanceToSqr(player);
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}
