package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCToolProperties;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCToolAttributeEvents {
    // Item keeps these UUIDs protected, but the tooltip recognizes them to display final main-hand values.
    private static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");

    private CCToolAttributeEvents() {
    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        CCToolProperties.Profile profile = CCToolProperties.get(event.getItemStack());
        if (profile == null) {
            return;
        }

        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            if (profile.attackDamage() != null) {
                event.removeAttribute(Attributes.ATTACK_DAMAGE);
                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BASE_ATTACK_DAMAGE_UUID, "CandyCraft configured attack damage",
                    profile.attackDamage() - 1.0D, AttributeModifier.Operation.ADDITION));
            }
            if (profile.attackSpeed() != null) {
                event.removeAttribute(Attributes.ATTACK_SPEED);
                event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(
                    BASE_ATTACK_SPEED_UUID, "CandyCraft configured attack speed",
                    profile.attackSpeed() - 4.0D, AttributeModifier.Operation.ADDITION));
            }
        }

        if (isConfiguredArmorSlot(profile.toolType(), event.getSlotType())) {
            applyEquipmentAttribute(event, Attributes.ARMOR, profile.armor(), "armor");
            applyEquipmentAttribute(event, Attributes.ARMOR_TOUGHNESS, profile.armorToughness(), "armor_toughness");
            applyEquipmentAttribute(event, Attributes.KNOCKBACK_RESISTANCE, profile.knockbackResistance(), "knockback_resistance");
        }
    }

    private static boolean isConfiguredArmorSlot(String toolType, EquipmentSlot slot) {
        return switch (toolType) {
            case "helmet" -> slot == EquipmentSlot.HEAD;
            case "chestplate" -> slot == EquipmentSlot.CHEST;
            case "leggings" -> slot == EquipmentSlot.LEGS;
            case "boots" -> slot == EquipmentSlot.FEET;
            default -> false;
        };
    }

    private static void applyEquipmentAttribute(ItemAttributeModifierEvent event,
            Attribute attribute, Double value, String key) {
        if (value == null) {
            return;
        }
        event.removeAttribute(attribute);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        UUID uuid = UUID.nameUUIDFromBytes(
            (CandyCraft.MODID + ":" + itemId + ":" + event.getSlotType() + ":" + key)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        event.addModifier(attribute, new AttributeModifier(
            uuid, "CandyCraft configured " + key, value, AttributeModifier.Operation.ADDITION));
    }
}
