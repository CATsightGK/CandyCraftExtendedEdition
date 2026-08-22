package com.valentin4311.candycraftmod.enchantment;

import com.valentin4311.candycraftmod.registry.CCItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class HoneySourceEnchantment extends Enchantment {
    public HoneySourceEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 12;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 25;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(CCItemTags.HONEY_SOURCE_TOOLS) || stack.is(CCItemTags.HONEY_SOURCE_RANGED);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return canEnchant(stack);
    }
}
