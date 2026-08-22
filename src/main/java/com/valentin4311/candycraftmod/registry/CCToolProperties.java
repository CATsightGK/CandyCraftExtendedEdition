package com.valentin4311.candycraftmod.registry;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.valentin4311.candycraftmod.CandyCraft;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public final class CCToolProperties {
    private static final String RESOURCE_PATH = "data/" + CandyCraft.MODID + "/tool_properties.json";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, Profile> PROFILES = loadProfiles();
    private static final Map<Item, Optional<Profile>> ITEM_CACHE = new ConcurrentHashMap<>();

    private CCToolProperties() {
    }

    public static Profile get(ItemStack stack) {
        return stack.isEmpty() ? null : get(stack.getItem());
    }

    public static Profile get(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null) {
            return null;
        }
        Optional<Profile> cached = ITEM_CACHE.get(item);
        if (cached != null) {
            return cached.orElse(null);
        }
        Optional<Profile> profile = Optional.ofNullable(PROFILES.get(id.toString()));
        ITEM_CACHE.put(item, profile);
        return profile.orElse(null);
    }

    public static Boolean configuredEnchantmentRule(ItemStack stack, Enchantment enchantment) {
        Profile profile = get(stack);
        if (profile == null || profile.useVanillaEnchantments()) {
            return null;
        }
        ResourceLocation id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        return id != null && profile.allowedEnchantments().contains(id.toString());
    }

    private static Map<String, Profile> loadProfiles() {
        try (InputStream stream = CCToolProperties.class.getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                LOGGER.warn("CandyCraft tool properties were not found at {}", RESOURCE_PATH);
                return Collections.emptyMap();
            }
            ToolPropertyFile file = new Gson().fromJson(
                new InputStreamReader(stream, StandardCharsets.UTF_8), ToolPropertyFile.class);
            return file == null || file.items == null ? Collections.emptyMap() : Map.copyOf(file.items);
        } catch (IOException | JsonParseException exception) {
            LOGGER.error("Unable to load CandyCraft tool properties", exception);
            return Collections.emptyMap();
        }
    }

    private static final class ToolPropertyFile {
        private Map<String, Profile> items;
    }

    public static final class Profile {
        private String category;
        private String toolType;
        private Integer durability;
        private Double attackDamage;
        private Double attackSpeed;
        private Double armor;
        private Double armorToughness;
        private Double knockbackResistance;
        private Integer enchantability;
        private Boolean useVanillaEnchantments;
        private List<String> allowedEnchantments;

        public String category() {
            return category == null ? "other" : category;
        }

        public String toolType() {
            return toolType == null ? "other" : toolType;
        }

        public Integer durability() {
            return durability == null ? null : Math.max(0, durability);
        }

        public Double attackDamage() {
            return finite(attackDamage);
        }

        public Double attackSpeed() {
            Double value = finite(attackSpeed);
            if (value != null && value < 0.0D && value > -4.0D) {
                value += 4.0D;
            }
            return value != null && value > 0.0D ? value : null;
        }

        public Double armor() {
            return finite(armor);
        }

        public Double armorToughness() {
            return finite(armorToughness);
        }

        public Double knockbackResistance() {
            return finite(knockbackResistance);
        }

        public Integer enchantability() {
            return enchantability == null ? null : Math.max(0, enchantability);
        }

        public boolean useVanillaEnchantments() {
            return Boolean.TRUE.equals(useVanillaEnchantments);
        }

        public List<String> allowedEnchantments() {
            return allowedEnchantments == null ? List.of() : allowedEnchantments;
        }

        private static Double finite(Double value) {
            return value != null && Double.isFinite(value) ? value : null;
        }
    }
}
