package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCCriteriaTriggers;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class CandyWolfEntity extends Wolf {
    private static final EntityDataAccessor<Integer> FUR_TIME = SynchedEntityData.defineId(CandyWolfEntity.class, EntityDataSerializers.INT);
    private static final String FUR_TIME_TAG = "Caramel";
    private static final int NORMAL_CARAMEL_TIME = 20 * 60 * 16;
    private static final int LEAVES_CARAMEL_TIME_MIN = 20 * 60 * 3;
    private static final int LEAVES_CARAMEL_TIME_RANGE = 20 * 60;

    public CandyWolfEntity(EntityType<? extends CandyWolfEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FUR_TIME, 0);
    }

    public int getFurTime() {
        return entityData.get(FUR_TIME);
    }

    public void setFurTime(int time) {
        entityData.set(FUR_TIME, Math.max(time, 0));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return isBrownie(stack);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide) {
            if (isTame() && getFurTime() <= 0 && random.nextInt(30) == 0) {
                for (int i = 0; i < 2; i++) {
                    level().addParticle(ParticleTypes.ENTITY_EFFECT, getRandomX(0.5D), getRandomY(), getRandomZ(0.5D), 0.8D, 0.3D, 0.0D);
                }
            }
            return;
        }

        if (isTame() && getFurTime() > 0) {
            setFurTime(getFurTime() - (isUnderCaramelLeaves() ? 5 : 1));
        }
    }

    private boolean isUnderCaramelLeaves() {
        BlockPos base = blockPosition();
        for (int y = 1; y <= 4; y++) {
            Block block = level().getBlockState(base.above(y)).getBlock();
            if (block == CCBlocks.CANDY_LEAVES_DARK.get()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isTame()) {
            if (stack.getItem() instanceof DyeItem dyeItem && dyeItem.getDyeColor() != getCollarColor()) {
                setCollarColor(dyeItem.getDyeColor());
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }

            if (stack.is(Items.BUCKET) && getFurTime() <= 0) {
                if (!level().isClientSide) {
                    ItemStack filled = new ItemStack(CCItems.CARAMEL_BUCKET.get());
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            player.setItemInHand(hand, filled);
                        } else if (!player.getInventory().add(filled)) {
                            player.drop(filled, false);
                        }
                    } else if (!player.getInventory().add(filled)) {
                        player.drop(filled, false);
                    }
                    resetCaramelTimer();
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        } else if (isBrownie(stack) && !isAngry()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (!level().isClientSide) {
                if (random.nextInt(3) == 0) {
                    tame(player);
                    setOrderedToSit(true);
                    navigation.stop();
                    setTarget(null);
                    setHealth(getMaxHealth());
                    resetCaramelTimer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        CCCriteriaTriggers.TAME_CANDY_WOLF.trigger(serverPlayer);
                    }
                    level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public Wolf getBreedOffspring(ServerLevel level, AgeableMob partner) {
        CandyWolfEntity child = CCEntityTypes.CANDY_WOLF.get().create(level);
        if (child != null && partner instanceof TamableAnimal tamable && isTame() && tamable.isTame()) {
            child.setOwnerUUID(getOwnerUUID());
            child.setTame(true);
            child.resetCaramelTimer();
        }
        return child;
    }

    @Override
    public void setTame(boolean tame) {
        super.setTame(tame);
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(tame ? 15.0D : 10.0D);
        }
        if (tame && getHealth() > 15.0F) {
            setHealth(15.0F);
        }
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (isTame() && target instanceof Sheep) {
            return false;
        }
        return super.wantsToAttack(target, owner);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (!CandyTargeting.canAttackEntity(target)) {
            setTarget(null);
            return false;
        }
        double damage = level().getDifficulty() == Difficulty.HARD ? 3.0D : 2.0D;
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        }
        return super.doHurtTarget(target);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFurTime(tag.getInt(FUR_TIME_TAG));
        setTame(isTame());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(FUR_TIME_TAG, getFurTime());
    }

    private boolean isBrownie(ItemStack stack) {
        return stack.is(CCItems.MILK_BROWNIE.get())
            || stack.is(CCItems.WHITE_BROWNIE.get())
            || stack.is(CCItems.DARK_BROWNIE.get());
    }

    private void resetCaramelTimer() {
        if (isUnderCaramelLeaves()) {
            setFurTime(LEAVES_CARAMEL_TIME_MIN + random.nextInt(LEAVES_CARAMEL_TIME_RANGE + 1));
        } else {
            setFurTime(NORMAL_CARAMEL_TIME);
        }
    }
}

