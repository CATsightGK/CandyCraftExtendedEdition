package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ThrownForkEntity extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> FORK_STACK = SynchedEntityData.defineId(ThrownForkEntity.class, EntityDataSerializers.ITEM_STACK);
    private float bonusDamage;

    public ThrownForkEntity(EntityType<? extends ThrownForkEntity> entityType, Level level) {
        super(entityType, level);
        setBaseDamage(8.0D);
    }

    public ThrownForkEntity(Level level, LivingEntity owner, ItemStack stack) {
        super(CCEntityTypes.THROWN_FORK.get(), owner, level);
        setBaseDamage(8.0D);
        setForkStack(stack);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FORK_STACK, new ItemStack(CCItems.FORK.get()));
    }

    @Override
    protected ItemStack getPickupItem() {
        return getForkStack().copy();
    }

    public ItemStack getForkStack() {
        ItemStack stack = entityData.get(FORK_STACK);
        return stack.isEmpty() ? new ItemStack(CCItems.FORK.get()) : stack;
    }

    public void setBonusDamage(float bonusDamage) {
        this.bonusDamage = Math.max(0.0F, bonusDamage);
        setBaseDamage(8.0D + this.bonusDamage);
    }

    private void setForkStack(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        entityData.set(FORK_STACK, copy);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Fork", getForkStack().save(new CompoundTag()));
        tag.putFloat("BonusDamage", bonusDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Fork", 10)) {
            setForkStack(ItemStack.of(tag.getCompound("Fork")));
        }
        if (tag.contains("BonusDamage")) {
            setBonusDamage(tag.getFloat("BonusDamage"));
        }
    }
}
