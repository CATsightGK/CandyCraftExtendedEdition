package com.valentin4311.candycraftmod.registry;

import java.util.EnumMap;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.util.Lazy;

public enum CCArmorMaterials implements ArmorMaterial {
    HONEY("honey", durability(12, 16, 15, 11), defense(2, 5, 6, 2), 18, () -> Ingredient.of(CCItems.HONEY_SHARD.get())),
    LICORICE("licorice", durability(15, 18, 16, 13), defense(2, 5, 6, 2), 12, () -> Ingredient.of(CCBlocks.LICORICE_BLOCK.get())),
    PEZ("pez", durability(18, 22, 20, 16), defense(3, 6, 8, 3), 14, () -> Ingredient.of(CCBlocks.PEZ_BLOCK.get())),
    JELLY("jelly", durability(11, 16, 15, 13), defense(2, 0, 0, 3), 20, () -> Ingredient.of(CCBlocks.JELLY_ORE.get())),
    MASK("mask", durability(11, 16, 15, 13), defense(0, 0, 0, 1), 20, () -> Ingredient.EMPTY);

    private final String name;
    private final EnumMap<ArmorItem.Type, Integer> durability;
    private final EnumMap<ArmorItem.Type, Integer> defense;
    private final int enchantmentValue;
    private final Lazy<Ingredient> repairIngredient;

    CCArmorMaterials(String name, EnumMap<ArmorItem.Type, Integer> durability, EnumMap<ArmorItem.Type, Integer> defense, int enchantmentValue, Supplier<Ingredient> repairIngredient) {
        this.name = name;
        this.durability = durability;
        this.defense = defense;
        this.enchantmentValue = enchantmentValue;
        this.repairIngredient = Lazy.of(repairIngredient);
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return durability.get(type);
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return defense.get(type);
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public net.minecraft.sounds.SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_GENERIC;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return "candycraftmod:" + name;
    }

    @Override
    public float getToughness() {
        return this == PEZ ? 1.0F : 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }

    private static EnumMap<ArmorItem.Type, Integer> durability(int boots, int leggings, int chestplate, int helmet) {
        EnumMap<ArmorItem.Type, Integer> values = new EnumMap<>(ArmorItem.Type.class);
        values.put(ArmorItem.Type.BOOTS, boots * 13);
        values.put(ArmorItem.Type.LEGGINGS, leggings * 13);
        values.put(ArmorItem.Type.CHESTPLATE, chestplate * 13);
        values.put(ArmorItem.Type.HELMET, helmet * 13);
        return values;
    }

    private static EnumMap<ArmorItem.Type, Integer> defense(int boots, int leggings, int chestplate, int helmet) {
        EnumMap<ArmorItem.Type, Integer> values = new EnumMap<>(ArmorItem.Type.class);
        values.put(ArmorItem.Type.BOOTS, boots);
        values.put(ArmorItem.Type.LEGGINGS, leggings);
        values.put(ArmorItem.Type.CHESTPLATE, chestplate);
        values.put(ArmorItem.Type.HELMET, helmet);
        return values;
    }
}
