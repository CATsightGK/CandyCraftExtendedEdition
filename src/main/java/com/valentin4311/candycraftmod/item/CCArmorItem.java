package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCArmorMaterials;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class CCArmorItem extends ArmorItem {
    public CCArmorItem(CCArmorMaterials material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        ArmorMaterial material = getMaterial();
        boolean innerModel = slot == EquipmentSlot.LEGS;
        String texture = material == CCArmorMaterials.JELLY
            ? (getType() == Type.HELMET ? "jelly_crown" : "armor_boots")
            : material == CCArmorMaterials.MASK
                ? "armor_mask"
                : "armor_" + material.getName().substring((CandyCraft.MODID + ":").length()) + "_" + (innerModel ? "2" : "1");
        return CandyCraft.MODID + ":textures/models/armor/" + texture + ".png";
    }
}
