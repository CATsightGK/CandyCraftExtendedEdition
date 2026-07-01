package com.valentin4311.candycraftmod.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NougatPowderItem extends Item {
    public NougatPowderItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        ItemStack result = super.finishUsingItem(stack, level, living);
        if (!level.isClientSide && living instanceof Player player && player.getRandom().nextInt(4) == 0) {
            level.explode(null, player.getX(), player.getY(), player.getZ(), 1.0F, Level.ExplosionInteraction.TNT);
        }
        return result;
    }
}
