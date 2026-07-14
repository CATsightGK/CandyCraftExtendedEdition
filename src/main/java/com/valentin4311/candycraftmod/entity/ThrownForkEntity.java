package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownForkEntity extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> FORK_STACK = SynchedEntityData.defineId(ThrownForkEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> EMBEDDED_IN_BLOCK = SynchedEntityData.defineId(
        ThrownForkEntity.class,
        EntityDataSerializers.BOOLEAN
    );
    private float bonusDamage;
    private boolean dealtDamage;

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
        entityData.define(EMBEDDED_IN_BLOCK, false);
    }

    @Override
    protected ItemStack getPickupItem() {
        return getForkStack().copy();
    }

    @Override
    public void tick() {
        if (inGroundTime > 4) {
            dealtDamage = true;
        }
        super.tick();
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 start, Vec3 end) {
        return dealtDamage ? null : super.findHitEntity(start, end);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        Entity target = hitResult.getEntity();
        float damage = (float)getBaseDamage();
        if (target instanceof LivingEntity livingTarget) {
            damage += EnchantmentHelper.getDamageBonus(getForkStack(), livingTarget.getMobType());
        }

        Entity owner = getOwner();
        dealtDamage = true;
        boolean hurt = target.hurt(damageSources().trident(this, owner == null ? this : owner), damage);
        if (hurt && target instanceof LivingEntity livingTarget) {
            if (target instanceof EnderMan) {
                return;
            }
            if (owner instanceof LivingEntity livingOwner) {
                EnchantmentHelper.doPostHurtEffects(livingTarget, livingOwner);
                EnchantmentHelper.doPostDamageEffects(livingOwner, livingTarget);
            }
            doPostHurtEffects(livingTarget);
        }

        setDeltaMovement(getDeltaMovement().multiply(-0.01D, -0.1D, -0.01D));
        playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        entityData.set(EMBEDDED_IN_BLOCK, true);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    protected float getWaterInertia() {
        return 0.99F;
    }

    public ItemStack getForkStack() {
        ItemStack stack = entityData.get(FORK_STACK);
        return stack.isEmpty() ? new ItemStack(CCItems.FORK.get()) : stack;
    }

    public boolean isEmbeddedInBlock() {
        return entityData.get(EMBEDDED_IN_BLOCK);
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
        tag.putBoolean("DealtDamage", dealtDamage);
        tag.putBoolean("EmbeddedInBlock", isEmbeddedInBlock());
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
        dealtDamage = tag.getBoolean("DealtDamage");
        entityData.set(
            EMBEDDED_IN_BLOCK,
            tag.contains("EmbeddedInBlock") ? tag.getBoolean("EmbeddedInBlock") : inGround
        );
    }
}
