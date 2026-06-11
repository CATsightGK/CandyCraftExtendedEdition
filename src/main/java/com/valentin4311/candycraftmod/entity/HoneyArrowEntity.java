package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HoneyArrowEntity extends AbstractArrow {
    private static final double CROSSBOW_DAMAGE_MULTIPLIER = 1.75D;
    private boolean crossbowDamageApplied;

    public HoneyArrowEntity(EntityType<? extends HoneyArrowEntity> entityType, Level level) {
        super(entityType, level);
    }

    public HoneyArrowEntity(Level level, LivingEntity owner) {
        super(CCEntityTypes.HONEY_ARROW.get(), owner, level);
    }

    public HoneyArrowEntity(Level level, double x, double y, double z) {
        super(CCEntityTypes.HONEY_ARROW.get(), x, y, z, level);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(CCItems.HONEY_ARROW.get());
    }

    @Override
    public void setShotFromCrossbow(boolean shotFromCrossbow) {
        super.setShotFromCrossbow(shotFromCrossbow);
        if (shotFromCrossbow && !crossbowDamageApplied) {
            setBaseDamage(getBaseDamage() * CROSSBOW_DAMAGE_MULTIPLIER);
            crossbowDamageApplied = true;
        }
    }
}
