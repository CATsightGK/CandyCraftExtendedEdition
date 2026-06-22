package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CandyPigEntity extends Pig {
    private static final double AVOID_DISTANCE = 7.0D;
    private static final List<java.util.function.Supplier<? extends net.minecraft.world.item.Item>> CANDY_CANE_DROPS = List.of(
        CCItems.CANDY_CANE,
        CCSweetscapeItems.WHITE_CANDY_CANE,
        CCSweetscapeItems.RED_CANDY_CANE,
        CCSweetscapeItems.GREEN_CANDY_CANE,
        CCSweetscapeItems.WHITE_GREEN_CANDY_CANE,
        CCSweetscapeItems.RED_GREEN_CANDY_CANE
    );

    public CandyPigEntity(EntityType<? extends CandyPigEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        goalSelector.addGoal(3, new BadgeTemptGoal(this, 1.2D, Ingredient.of(CCItems.DRAGIBUS_STICK.get(), CCItems.DRAGIBUS.get())));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        goalSelector.addGoal(5, new BadgeAvoidGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.DRAGIBUS.get());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Player threat = level().getNearestPlayer(this, AVOID_DISTANCE);
        boolean avoiding = threat != null && shouldAvoidPlayer(threat);
        setSprinting(avoiding);
        if (avoiding && level().isClientSide && random.nextInt(3) == 0) {
            level().addParticle(ParticleTypes.CLOUD,
                getX() + (random.nextDouble() - 0.5D) * getBbWidth(),
                getY() + 0.1D,
                getZ() + (random.nextDouble() - 0.5D) * getBbWidth(),
                -getDeltaMovement().x * 0.5D,
                0.02D,
                -getDeltaMovement().z * 0.5D);
        }
    }

    @Override
    public net.minecraft.world.entity.LivingEntity getControllingPassenger() {
        Entity entity = super.getControllingPassenger();
        if (!(entity instanceof Player player)) {
            return null;
        }
        return hasDragibusStick(player) ? player : null;
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        return hasDragibusStick(player) ? super.getRiddenInput(player, travelVector) : Vec3.ZERO;
    }

    @Override
    public boolean boost() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (isBaby()) {
            return;
        }
        int count = 1 + random.nextInt(3) + random.nextInt(looting + 1);
        for (int i = 0; i < count; i++) {
            spawnAtLocation(CANDY_CANE_DROPS.get(random.nextInt(CANDY_CANE_DROPS.size())).get());
        }
    }

    private boolean shouldAvoidPlayer(Player player) {
        return !player.isSpectator()
            && !EmblemHelper.has(player, CCItems.GINGERBREAD_EMBLEM.get());
    }

    private boolean hasDragibusStick(Player player) {
        return player.getMainHandItem().is(CCItems.DRAGIBUS_STICK.get())
            || player.getOffhandItem().is(CCItems.DRAGIBUS_STICK.get());
    }

    @Nullable
    @Override
    public Pig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return CCEntityTypes.CANDY_PIG.get().create(level);
    }

    private static final class BadgeTemptGoal extends TemptGoal {
        private final CandyPigEntity pig;

        private BadgeTemptGoal(CandyPigEntity pig, double speedModifier, Ingredient items) {
            super(pig, speedModifier, items, false);
            this.pig = pig;
        }

        @Override
        public boolean canUse() {
            Player player = pig.level().getNearestPlayer(pig, 10.0D);
            return player != null && !pig.shouldAvoidPlayer(player) && super.canUse();
        }
    }

    private static final class BadgeAvoidGoal extends Goal {
        private final CandyPigEntity pig;
        private Player threat;
        private Vec3 wanted;

        private BadgeAvoidGoal(CandyPigEntity pig) {
            this.pig = pig;
            setFlags(java.util.EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            threat = pig.level().getNearestPlayer(pig, AVOID_DISTANCE);
            if (threat == null || !pig.shouldAvoidPlayer(threat)) {
                return false;
            }
            wanted = DefaultRandomPos.getPosAway(pig, 16, 7, threat.position());
            if (wanted == null) {
                Vec3 away = pig.position().subtract(threat.position()).normalize().scale(6.0D);
                wanted = pig.position().add(away);
            }
            return wanted != null;
        }

        @Override
        public boolean canContinueToUse() {
            return threat != null && pig.shouldAvoidPlayer(threat) && pig.distanceToSqr(threat) < AVOID_DISTANCE * AVOID_DISTANCE * 1.8D
                && !pig.getNavigation().isDone();
        }

        @Override
        public void start() {
            pig.setSprinting(true);
            pig.getNavigation().moveTo(wanted.x, wanted.y, wanted.z, 1.15D);
        }

        @Override
        public void tick() {
            if (threat != null && pig.tickCount % 10 == 0) {
                Vec3 next = DefaultRandomPos.getPosAway(pig, 16, 7, threat.position());
                if (next != null) {
                    wanted = next;
                    pig.getNavigation().moveTo(wanted.x, wanted.y, wanted.z, 1.15D);
                }
            }
        }

        @Override
        public void stop() {
            threat = null;
            wanted = null;
            pig.setSprinting(false);
        }
    }
}
