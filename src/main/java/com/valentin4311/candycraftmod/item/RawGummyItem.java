package com.valentin4311.candycraftmod.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RawGummyItem extends Item {
    public static final int TRIP_DURATION_TICKS = 30;
    private static final float TRIP_EFFECT_CHANCE = 0.9F;

    public RawGummyItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, level, living);
        if (!level.isClientSide && level.random.nextFloat() < TRIP_EFFECT_CHANCE) {
            living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, TRIP_DURATION_TICKS, 0, false, true, true));
        }
        return result;
    }
}
