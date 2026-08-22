package com.valentin4311.candycraftmod.enchantment;

import com.valentin4311.candycraftmod.item.ForkItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class ForkEnchantment extends Enchantment {
    private final int maxLevel;
    private final int baseCost;
    private final boolean treasureOnly;

    public ForkEnchantment(Rarity rarity, int maxLevel, int baseCost, boolean treasureOnly) {
        super(rarity, EnchantmentCategory.WEAPON, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.treasureOnly = treasureOnly;
    }

    @Override
    public int getMinCost(int level) {
        return baseCost + (level - 1) * 10;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 25;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof ForkItem;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return !treasureOnly && canEnchant(stack);
    }

    @Override
    public boolean isTreasureOnly() {
        return treasureOnly;
    }

    @Override
    public boolean isTradeable() {
        return !treasureOnly;
    }

    @Override
    public boolean isDiscoverable() {
        return !treasureOnly;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return !(other instanceof ForkEnchantment) && super.checkCompatibility(other);
    }
}
