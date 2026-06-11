package com.valentin4311.candycraftmod.alchemy;

import com.valentin4311.candycraftmod.item.SugarPillItem;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AlchemyMixing {
    public static final int INPUT_SLOTS = 4;
    private static final int MINUTE = 20 * 60;
    private static final int COLOR_RED = 0xD94141;
    private static final int COLOR_YELLOW = 0xE2C64D;
    private static final int COLOR_GRAY = 0x8E8E8E;
    private static final int COLOR_PURPLE = 0x8A47C7;
    private static final int COLOR_GREEN = 0x63B552;
    private static final int COLOR_BLUE = 0x4C74D9;
    private static final int COLOR_BROWN = 0x7D5635;
    private static final int COLOR_ORANGE = 0xDA8B31;
    private static final int COLOR_CYAN = 0x5ABFC1;
    private static final int COLOR_MAGENTA = 0xD44FD6;
    private static final int COLOR_BLACK = 0x2D2D2D;
    private static final int COLOR_DARK_BLUE = 0x2D4F9E;
    private static final int COLOR_GRAY_BLUE = 0x7B8AA8;
    private static final Map<Item, IngredientEffect> RECIPES = new HashMap<>();

    static {
        register(CCItems.COTTON_CANDY.get(), COLOR_RED, MobEffects.SATURATION);
        register(CCItems.HONEY_SHARD.get(), COLOR_YELLOW, MobEffects.DIG_SPEED);
        register(CCItems.SUGAR_CRYSTAL.get(), COLOR_GRAY, MobEffects.INVISIBILITY);
        register(CCItems.NOUGAT_POWDER.get(), COLOR_PURPLE, MobEffects.GLOWING);
        register(CCItems.LOLLIPOP_SEEDS.get(), COLOR_GRAY, MobEffects.WEAKNESS);
        register(CCItems.WAFFLE_NUGGET.get(), COLOR_GREEN, MobEffects.HUNGER);
        register(Items.SUGAR, COLOR_GREEN, MobEffects.HUNGER);
        register(CCItems.WAFFLE.get(), COLOR_BLUE, MobEffects.ABSORPTION);
        register(CCItems.GUMMY.get(), COLOR_PURPLE, MobEffects.JUMP);
        register(CCItems.CARAMEL_BUCKET.get(), COLOR_RED, MobEffects.DAMAGE_BOOST);
        register(CCItems.CRANBERRY_FISH.get(), COLOR_BROWN, MobEffects.HARM);
        register(CCItems.CANDIED_CHERRY.get(), COLOR_RED, MobEffects.HEAL);
        register(CCItems.LOLLIPOP.get(), COLOR_RED, MobEffects.HEAL);
        register(CCItems.CHEWING_GUM.get(), COLOR_BROWN, MobEffects.DIG_SLOWDOWN);
        register(CCItems.PEZ_DUST.get(), COLOR_ORANGE, MobEffects.FIRE_RESISTANCE);
        register(CCItems.PEZ.get(), COLOR_BROWN, MobEffects.DAMAGE_RESISTANCE);
        register(CCItems.HONEYCOMB.get(), COLOR_CYAN, MobEffects.MOVEMENT_SPEED);
        register(CCItems.MARSHMALLOW_FLOWER.get(), COLOR_CYAN, MobEffects.MOVEMENT_SPEED);
        register(CCItems.DRAGIBUS.get(), COLOR_MAGENTA, MobEffects.REGENERATION);
        register(CCItems.CANDY_CANE.get(), COLOR_MAGENTA, MobEffects.REGENERATION);
        register(Items.COOKIE, COLOR_BLACK, MobEffects.BLINDNESS);
        register(CCItems.LICORICE.get(), COLOR_DARK_BLUE, MobEffects.NIGHT_VISION);
        register(CCItems.GUMMY_BALL.get(), COLOR_GRAY_BLUE, MobEffects.MOVEMENT_SLOWDOWN);
    }

    private AlchemyMixing() {
    }

    public static boolean isValidIngredient(ItemStack stack) {
        return !stack.isEmpty() && RECIPES.containsKey(stack.getItem());
    }

    public static ItemStack craft(List<ItemStack> inputs) {
        if (inputs.size() != INPUT_SLOTS) {
            return ItemStack.EMPTY;
        }

        Map<Item, Integer> counts = new LinkedHashMap<>();
        List<Integer> colors = new ArrayList<>();
        for (ItemStack stack : inputs) {
            if (!isValidIngredient(stack)) {
                return ItemStack.EMPTY;
            }
            Item item = stack.getItem();
            counts.merge(item, 1, Integer::sum);
            colors.add(RECIPES.get(item).color());
        }

        List<MobEffectInstance> effects = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            IngredientEffect recipe = RECIPES.get(entry.getKey());
            EffectStrength strength = effectStrength(entry.getValue());
            effects.add(new MobEffectInstance(recipe.effect(), strength.durationTicks(), strength.amplifier(), false, true, true));
        }

        ItemStack pill = new ItemStack(CCItems.SUGAR_PILL.get());
        SugarPillItem.setData(pill, effects, normalizeColors(colors));
        return pill;
    }

    public static int[] normalizeColors(List<Integer> colors) {
        int[] result = new int[4];
        Arrays.fill(result, 0xFFFFFF);
        for (int i = 0; i < Math.min(4, colors.size()); i++) {
            result[i] = colors.get(i);
        }
        return result;
    }

    private static EffectStrength effectStrength(int count) {
        return switch (count) {
            case 1 -> new EffectStrength(0, MINUTE);
            case 2 -> new EffectStrength(0, 2 * MINUTE);
            case 3 -> new EffectStrength(1, 2 * MINUTE);
            case 4 -> new EffectStrength(1, 3 * MINUTE);
            default -> new EffectStrength(0, MINUTE);
        };
    }

    private static void register(Item item, int color, MobEffect effect) {
        RECIPES.put(item, new IngredientEffect(color, effect));
    }

    private record IngredientEffect(int color, MobEffect effect) {
    }

    private record EffectStrength(int amplifier, int durationTicks) {
    }
}
