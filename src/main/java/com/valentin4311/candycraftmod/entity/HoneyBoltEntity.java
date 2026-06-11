package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HoneyBoltEntity extends AbstractArrow {
    private static final double CROSSBOW_DAMAGE = 4.0D;

    public HoneyBoltEntity(EntityType<? extends HoneyBoltEntity> entityType, Level level) {
        super(entityType, level);
        setBaseDamage(CROSSBOW_DAMAGE);
    }

    public HoneyBoltEntity(Level level, LivingEntity owner) {
        super(CCEntityTypes.HONEY_BOLT.get(), owner, level);
        setBaseDamage(CROSSBOW_DAMAGE);
    }

    public HoneyBoltEntity(Level level, double x, double y, double z) {
        super(CCEntityTypes.HONEY_BOLT.get(), x, y, z, level);
        setBaseDamage(CROSSBOW_DAMAGE);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(CCItems.HONEY_BOLT.get());
    }
}
