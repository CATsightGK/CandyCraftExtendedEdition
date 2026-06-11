package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.entity.HoneyArrowEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HoneyArrowItem extends ArrowItem {
    public HoneyArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack stack, LivingEntity shooter) {
        return new HoneyArrowEntity(level, shooter);
    }
}
