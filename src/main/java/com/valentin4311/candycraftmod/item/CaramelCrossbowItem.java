package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.entity.HoneyBoltEntity;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class CaramelCrossbowItem extends CrossbowItem {
    public static final int HONEY_CHARGE_DURATION = 25;
    private static final Predicate<ItemStack> HONEY_BOLTS = stack -> stack.is(CCItems.HONEY_BOLT.get());

    public CaramelCrossbowItem(Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return HONEY_BOLTS;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return HONEY_BOLTS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack crossbow = player.getItemInHand(hand);
        if (isCharged(crossbow)) {
            shootHoneyBolts(level, player, hand, crossbow, 3.15F, 1.0F);
            setCharged(crossbow, false);
            clearChargedProjectiles(crossbow);
            return InteractionResultHolder.consume(crossbow);
        }

        if (!player.getProjectile(crossbow).isEmpty() || player.getAbilities().instabuild) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(crossbow);
        }

        return InteractionResultHolder.fail(crossbow);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int usedTicks = getUseDuration(stack) - timeLeft;
        if (getPowerForTime(usedTicks) >= 1.0F && !isCharged(stack) && tryLoadHoneyBolts(entity, stack)) {
            setCharged(stack, true);
            SoundSource source = entity instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CROSSBOW_LOADING_END, source, 1.0F, 1.0F);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return HONEY_CHARGE_DURATION + 3;
    }

    private static float getPowerForTime(int useTicks) {
        return Math.min((float)useTicks / (float)HONEY_CHARGE_DURATION, 1.0F);
    }

    private static boolean tryLoadHoneyBolts(LivingEntity entity, ItemStack crossbow) {
        int multishot = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow);
        int projectileCount = multishot == 0 ? 1 : 3;
        boolean creative = entity instanceof Player player && player.getAbilities().instabuild;
        ItemStack ammo = entity.getProjectile(crossbow);

        if (ammo.isEmpty() && creative) {
            ammo = new ItemStack(CCItems.HONEY_BOLT.get());
        }

        if (ammo.isEmpty() || !ammo.is(CCItems.HONEY_BOLT.get())) {
            return false;
        }

        ItemStack template = ammo.copy();
        for (int i = 0; i < projectileCount; ++i) {
            ItemStack loaded = template.copy();
            loaded.setCount(1);
            addChargedProjectile(crossbow, loaded);
        }

        if (!creative) {
            ammo.shrink(1);
            if (ammo.isEmpty() && entity instanceof Player player) {
                player.getInventory().removeItem(ammo);
            }
        }

        return true;
    }

    private static void shootHoneyBolts(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbow, float velocity, float inaccuracy) {
        List<ItemStack> projectiles = getChargedProjectiles(crossbow);
        if (projectiles.isEmpty()) {
            if (shooter instanceof Player player && player.getAbilities().instabuild) {
                projectiles.add(new ItemStack(CCItems.HONEY_BOLT.get()));
            } else {
                return;
            }
        }

        float[] angles = projectiles.size() == 3 ? new float[] { 0.0F, -10.0F, 10.0F } : new float[] { 0.0F };
        for (int i = 0; i < projectiles.size(); ++i) {
            shootHoneyBolt(level, shooter, crossbow, projectiles.get(i), velocity, inaccuracy, angles[Math.min(i, angles.length - 1)]);
        }

        crossbow.hurtAndBreak(projectiles.size(), shooter, living -> living.broadcastBreakEvent(hand));
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (shooter instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.ITEM_USED.get(crossbow.getItem()));
        }
    }

    private static void shootHoneyBolt(Level level, LivingEntity shooter, ItemStack crossbow, ItemStack projectileStack, float velocity, float inaccuracy, float yawOffset) {
        if (level.isClientSide) {
            return;
        }

        HoneyBoltEntity bolt = new HoneyBoltEntity(level, shooter);
        bolt.setShotFromCrossbow(true);
        bolt.setCritArrow(true);
        int piercing = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, crossbow);
        if (piercing > 0) {
            bolt.setPierceLevel((byte)piercing);
        }
        bolt.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yawOffset, 0.0F, velocity, inaccuracy);
        level.addFreshEntity(bolt);
    }

    private static void addChargedProjectile(ItemStack crossbow, ItemStack projectile) {
        CompoundTag tag = crossbow.getOrCreateTag();
        ListTag projectiles = tag.getList("ChargedProjectiles", 10);
        CompoundTag projectileTag = new CompoundTag();
        projectile.save(projectileTag);
        projectiles.add(projectileTag);
        tag.put("ChargedProjectiles", projectiles);
    }

    private static List<ItemStack> getChargedProjectiles(ItemStack crossbow) {
        List<ItemStack> projectiles = new ArrayList<>();
        CompoundTag tag = crossbow.getTag();
        if (tag != null && tag.contains("ChargedProjectiles", 9)) {
            ListTag projectileTags = tag.getList("ChargedProjectiles", 10);
            for (int i = 0; i < projectileTags.size(); ++i) {
                projectiles.add(ItemStack.of(projectileTags.getCompound(i)));
            }
        }
        return projectiles;
    }

    private static void clearChargedProjectiles(ItemStack crossbow) {
        CompoundTag tag = crossbow.getTag();
        if (tag != null) {
            tag.remove("ChargedProjectiles");
        }
    }
}
