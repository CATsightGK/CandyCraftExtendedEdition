package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class PingouinEntity extends Animal {
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(PingouinEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SUPER = SynchedEntityData.defineId(PingouinEntity.class, EntityDataSerializers.BOOLEAN);

    public PingouinEntity(EntityType<? extends PingouinEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 8.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.BreedGoal(this, 0.5D));
        goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.TemptGoal(this, 0.5D, net.minecraft.world.item.crafting.Ingredient.of(CCItems.CRANBERRY_FISH.get()), false));
        goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.TemptGoal(this, 0.5D, net.minecraft.world.item.crafting.Ingredient.of(CCItems.MARSHMALLOW_FLOWER.get()), false));
        goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.FollowParentGoal(this, 0.28D));
        goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 0.5D));
        goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, 0);
        entityData.define(SUPER, false);
    }

    public int getColor() {
        return entityData.get(COLOR) & 3;
    }

    public void setColor(int color) {
        entityData.set(COLOR, color & 3);
    }

    public boolean isSuperPingouin() {
        return entityData.get(SUPER);
    }

    public void setSuper(boolean value) {
        entityData.set(SUPER, value);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!onGround() && getDeltaMovement().y < 0.0D) {
            setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.CRANBERRY_FISH.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(CCItems.MARSHMALLOW_FLOWER.get())) {
            if (level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            spawnAtLocation(new ItemStack(getIceCreamDrop(), random.nextInt(6) + 5));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        setColor(random.nextInt(3));
        if (random.nextInt(30) == 0) {
            setSuper(true);
        }
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Super", isSuperPingouin());
        tag.putInt("Color", getColor());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSuper(tag.getBoolean("Super"));
        setColor(tag.getInt("Color"));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        PingouinEntity child = CCEntityTypes.PINGOUIN.get().create(level);
        if (child != null) {
            child.setColor(random.nextBoolean() || !(partner instanceof PingouinEntity pingouin) ? getColor() : pingouin.getColor());
        }
        return child;
    }

    @Override
    public int getExperienceReward() {
        return 1 + random.nextInt(3);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    private Block getIceCreamDrop() {
        return switch (getColor()) {
            case 0 -> CCBlocks.STRAWBERRY_ICE_CREAM.get();
            case 1 -> CCBlocks.MINT_ICE_CREAM.get();
            case 2 -> CCBlocks.BLUEBERRY_ICE_CREAM.get();
            default -> CCBlocks.ICE_CREAM.get();
        };
    }
}
