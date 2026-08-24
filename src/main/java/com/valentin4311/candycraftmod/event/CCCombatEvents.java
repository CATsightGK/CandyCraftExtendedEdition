package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.CaramelBeeEntity;
import com.valentin4311.candycraftmod.entity.HoneyArrowEntity;
import com.valentin4311.candycraftmod.entity.HoneyBoltEntity;
import com.valentin4311.candycraftmod.registry.CCEnchantments;
import com.valentin4311.candycraftmod.registry.CCItemTags;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMobEffects;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Damage pipeline: propolis/honey-source offense, jelly boots and emblem
 * defenses. All LivingHurtEvent adjustments stay in one listener so the
 * add-then-multiply order remains deterministic.
 */
@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCCombatEvents {
    private static final String HONEY_SOURCE_LEVEL = CandyCraft.MODID + ".honey_source_level";

    private CCCombatEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide) {
            markHoneySourceProjectile(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.level().isClientSide) {
            applyPropolisDamageBonuses(event);
            applyHoneySourceEffect(event);
        }

        if (!(target instanceof Player player)) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.IS_FALL) && player.getItemBySlot(EquipmentSlot.FEET).is(CCItems.JELLY_BOOTS.get())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }
        if (event.getSource().is(DamageTypeTags.IS_FALL) && EmblemHelper.has(player, CCItems.JELLY_EMBLEM.get())) {
            event.setAmount(event.getAmount() * 0.7F);
        }
        if (event.getSource().getDirectEntity() instanceof AbstractArrow && EmblemHelper.has(player, CCItems.SUGUARD_EMBLEM.get())) {
            event.setAmount(event.getAmount() * 0.8F);
        }
    }

    private static void markHoneySourceProjectile(Entity entity) {
        if (!(entity instanceof Projectile projectile)
                || !(projectile.getOwner() instanceof LivingEntity owner)) {
            return;
        }

        int level = Math.max(
            honeySourceLevel(owner.getMainHandItem(), CCItemTags.HONEY_SOURCE_RANGED),
            honeySourceLevel(owner.getOffhandItem(), CCItemTags.HONEY_SOURCE_RANGED)
        );
        if (level > 0) {
            entity.getPersistentData().putInt(HONEY_SOURCE_LEVEL, level);
        }
    }

    private static void applyPropolisDamageBonuses(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(CCMobEffects.HONEY_GLUE.get())) {
            return;
        }

        Entity direct = event.getSource().getDirectEntity();
        Entity attacker = event.getSource().getEntity();
        float amount = event.getAmount();
        if (direct instanceof HoneyArrowEntity) {
            amount += 2.0F;
        } else if (direct instanceof HoneyBoltEntity) {
            amount += 2.5F;
        } else if (attacker instanceof LivingEntity livingAttacker
                && direct == attacker
                && livingAttacker.getMainHandItem().is(CCItemTags.PROPOLIS_BONUS_TOOLS)) {
            amount += 2.0F;
        }

        if (attacker instanceof CaramelBeeEntity || attacker instanceof Bee) {
            amount *= 2.0F;
        }
        event.setAmount(amount);
    }

    private static void applyHoneySourceEffect(LivingHurtEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        Entity attacker = event.getSource().getEntity();
        int level = direct == null ? 0 : direct.getPersistentData().getInt(HONEY_SOURCE_LEVEL);
        int duration = level > 0 ? 10 * 20 : 0;

        if (level <= 0 && attacker instanceof LivingEntity livingAttacker && direct == attacker) {
            level = honeySourceLevel(livingAttacker.getMainHandItem(), CCItemTags.HONEY_SOURCE_TOOLS);
            duration = level > 0 ? 7 * 20 : 0;
        }
        if (level <= 0) {
            return;
        }

        MobEffectInstance propolis = new MobEffectInstance(
            CCMobEffects.HONEY_GLUE.get(), duration, Math.min(1, level - 1), false, true, true
        );
        if (attacker != null) {
            event.getEntity().addEffect(propolis, attacker);
        } else {
            event.getEntity().addEffect(propolis);
        }
    }

    private static int honeySourceLevel(ItemStack stack, TagKey<Item> allowedItems) {
        if (!stack.is(allowedItems)) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(CCEnchantments.HONEY_SOURCE.get(), stack);
    }
}
