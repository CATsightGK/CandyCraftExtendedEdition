package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EasterChickenEntity extends Animal {
    private static final List<java.util.function.Supplier<Item>> CHOCOLATE_EGGS = List.of(
        CCItems.MILK_CHOCOLATE_EGG,
        CCItems.WHITE_CHOCOLATE_EGG,
        CCItems.DARK_CHOCOLATE_EGG
    );

    public float wingRotation;
    public float destPos;
    public float oFlapSpeed;
    public float oFlap;
    public boolean explodeWhenDone;
    private float wingRotDelta = 1.0F;
    public int timeUntilNextEgg = random.nextInt(6000) + 6000;
    private int nextEggType = -1;
    private int eggComboAmount;

    public EasterChickenEntity(EntityType<? extends EasterChickenEntity> type, Level level) {
        super(type, level);
        setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.3D));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        goalSelector.addGoal(3, new TemptGoal(this, 1.0D, Ingredient.of(
            CCItems.WAFER_STICK.get(),
            CCItems.MILK_CHOCOLATE_BAR.get(),
            CCItems.WHITE_CHOCOLATE_BAR.get(),
            CCItems.DARK_CHOCOLATE_BAR.get()), false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isBaby() && nextEggType == -1 && !explodeWhenDone) {
            if (stack.is(Items.FIRE_CHARGE)) {
                firePanic();
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            int eggType = chocolateType(stack);
            if (eggType >= 0) {
                if (!level().isClientSide) {
                    timeUntilNextEgg = 30 + random.nextInt(30);
                    nextEggType = eggType;
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
                for (int i = 0; i < 6; i++) {
                    level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        getX() + (random.nextDouble() - 0.5D) * getBbWidth(),
                        getY() + 0.5D + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5D) * getBbWidth(),
                        0.0D, 0.0D, 0.0D);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void spawnSprintParticle() {
        level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        oFlap = wingRotation;
        oFlapSpeed = destPos;
        destPos = Mth.clamp(destPos + (onGround() ? -1.0F : 4.0F) * 0.3F, 0.0F, 1.0F);
        if (!onGround() && wingRotDelta < 1.0F) {
            wingRotDelta = 1.0F;
        }
        wingRotDelta *= 0.9F;
        Vec3 movement = getDeltaMovement();
        if (!onGround() && movement.y < 0.0D) {
            setDeltaMovement(movement.multiply(1.0D, 0.6D, 1.0D));
        }
        wingRotation += wingRotDelta * 2.0F;

        if (!level().isClientSide && !isBaby() && --timeUntilNextEgg <= 0) {
            boolean forced = nextEggType != -1;
            int eggType = forced ? nextEggType : random.nextInt(CHOCOLATE_EGGS.size());
            nextEggType = -1;
            playSound(SoundEvents.CHICKEN_EGG, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
            spawnAtLocation(new ItemStack(CHOCOLATE_EGGS.get(eggType).get()));

            if (eggComboAmount <= 0 && !forced && random.nextInt(100) == 0) {
                eggComboAmount = 30 + random.nextInt(30);
                hurt(level().damageSources().generic(), 0.0F);
            }
            if (eggComboAmount-- > 0) {
                timeUntilNextEgg = 1;
            } else {
                timeUntilNextEgg = random.nextInt(6000) + 10000;
            }
            if (eggComboAmount == 0 && explodeWhenDone) {
                explodeIntoDrops();
            }
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.WAFER_STICK.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return CCEntityTypes.EASTER_CHICKEN.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CHICKEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CHICKEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CHICKEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("EggLayTime", timeUntilNextEgg);
        tag.putInt("NextEggType", nextEggType);
        tag.putInt("EggComboAmount", eggComboAmount);
        tag.putBoolean("ExplodeWhenDone", explodeWhenDone);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        timeUntilNextEgg = tag.getInt("EggLayTime");
        nextEggType = tag.contains("NextEggType") ? tag.getInt("NextEggType") : -1;
        eggComboAmount = tag.getInt("EggComboAmount");
        explodeWhenDone = tag.getBoolean("ExplodeWhenDone");
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 4.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    public static boolean canSpawn(EntityType<? extends EasterChickenEntity> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        return level.getRawBrightness(pos, 0) > 8
            && (below.is(CCBlocks.CANDY_GRASS_BLOCK.get())
                || below.is(CCBlocks.DARK_CANDY_GRASS_BLOCK.get())
                || below.is(CCBlocks.CHOCOLATE_COVERED_WHITE_BROWNIE.get())
                || below.is(CCBlocks.WHITE_BROWNIE_BLOCK.get())
                || below.is(CCBlocks.MILK_BROWNIE_BLOCK.get())
                || below.is(CCBlocks.DARK_BROWNIE_BLOCK.get()));
    }

    private void firePanic() {
        if (!level().isClientSide) {
            timeUntilNextEgg = 20 + random.nextInt(50);
            explodeWhenDone = true;
            setSprinting(true);
            eggComboAmount = 25 + random.nextInt(20);
            playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
        }
    }

    private void explodeIntoDrops() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        playSound(SoundEvents.GENERIC_EXPLODE, 1.0F, 1.0F);
        for (int i = 0; i < 3 + random.nextInt(6); i++) {
            spawnAtLocation(Items.FEATHER);
        }
        for (int i = 0; i < 3 + random.nextInt(4); i++) {
            spawnAtLocation(CHOCOLATE_EGGS.get(random.nextInt(CHOCOLATE_EGGS.size())).get());
        }
        discard();
    }

    private int chocolateType(ItemStack stack) {
        if (stack.is(CCItems.MILK_CHOCOLATE_BAR.get())) {
            return 0;
        }
        if (stack.is(CCItems.WHITE_CHOCOLATE_BAR.get())) {
            return 1;
        }
        if (stack.is(CCItems.DARK_CHOCOLATE_BAR.get())) {
            return 2;
        }
        return -1;
    }
}

