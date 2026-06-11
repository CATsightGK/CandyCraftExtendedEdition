package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
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
        return stack.is(CCItems.CANDY_CANE.get());
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
            setFurTime(getFurTime() - (isUnderCandyLeaves() ? 2 : 1));
        }
    }

    private boolean isUnderCandyLeaves() {
        BlockPos base = blockPosition();
        for (int y = 0; y < 4; y++) {
            Block block = level().getBlockState(base.above(y)).getBlock();
            if (block == CCBlocks.CANDY_LEAVES.get()
                || block == CCBlocks.CANDY_LEAVES_DARK.get()
                || block == CCBlocks.CANDY_LEAVES_LIGHT.get()) {
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
                    setFurTime(random.nextInt(12000) + 5000);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        } else if (stack.is(CCItems.CANDY_CANE.get()) && !isAngry()) {
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
                    setFurTime(random.nextInt(12000) + 10000);
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
            child.setFurTime(level.random.nextInt(6000) + 5000);
        }
        return child;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(FUR_TIME_TAG, getFurTime());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setFurTime(tag.getInt(FUR_TIME_TAG));
    }
}
