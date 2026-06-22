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
        String texture;
        if (material == CCArmorMaterials.JELLY) {
            texture = getType() == Type.HELMET ? "jelly_crown" : "armor_boots";
        } else if (material == CCArmorMaterials.MASK) {
            texture = "armor_mask";
        } else {
            boolean leggings = slot == EquipmentSlot.LEGS;
            String materialName = material.getName().substring((CandyCraft.MODID + ":").length());
            texture = "armor_" + materialName + "_" + (leggings ? "1" : "2");
        }
        return CandyCraft.MODID + ":textures/models/armor/" + texture + ".png";
    }
}
