package com.valentin4311.candycraftmod.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class SugarPillItem extends Item {
    private static final String TAG_EFFECTS = "Effects";
    private static final String TAG_COLORS = "Colors";

    public SugarPillItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide && livingEntity instanceof Player player) {
            player.removeAllEffects();
            for (MobEffectInstance effect : getEffects(stack)) {
                player.addEffect(new MobEffectInstance(effect));
            }
        }

        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (MobEffectInstance effect : getEffects(stack)) {
            Component name = Component.translatable(effect.getDescriptionId());
            int amplifier = effect.getAmplifier();
            if (amplifier > 0) {
                name = Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + amplifier));
            }
            name = Component.translatable("potion.withDuration", name, MobEffectUtilCompat.formatDuration(effect));
            tooltip.add(name.copy().withStyle(effect.getEffect().getCategory().getTooltipFormatting()));
        }
        if (getEffects(stack).isEmpty()) {
            tooltip.add(Component.translatable("item.candycraftmod.sugar_pill.empty").withStyle(ChatFormatting.GRAY));
        }
    }

    public static List<MobEffectInstance> getEffects(ItemStack stack) {
        List<MobEffectInstance> effects = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_EFFECTS, Tag.TAG_LIST)) {
            return effects;
        }

        ListTag list = tag.getList(TAG_EFFECTS, Tag.TAG_COMPOUND);
        for (Tag element : list) {
            if (element instanceof CompoundTag effectTag) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(net.minecraft.resources.ResourceLocation.tryParse(effectTag.getString("Id")));
                if (effect != null) {
                    effects.add(new MobEffectInstance(effect, effectTag.getInt("Duration"), effectTag.getInt("Amplifier"), false, true, true));
                }
            }
        }
        return effects;
    }

    public static int getLayerColor(ItemStack stack, int tintIndex) {
        if (tintIndex <= 0) {
            return -1;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_COLORS, Tag.TAG_INT_ARRAY)) {
            return -1;
        }

        int[] colors = tag.getIntArray(TAG_COLORS);
        int colorIndex = tintIndex - 1;
        return colorIndex < colors.length ? colors[colorIndex] : 0xFFFFFF;
    }

    public static void setData(ItemStack stack, List<MobEffectInstance> effects, int[] colors) {
        CompoundTag tag = stack.getOrCreateTag();
        ListTag list = new ListTag();
        for (MobEffectInstance effect : effects) {
            CompoundTag effectTag = new CompoundTag();
            effectTag.putString("Id", ForgeRegistries.MOB_EFFECTS.getKey(effect.getEffect()).toString());
            effectTag.putInt("Duration", effect.getDuration());
            effectTag.putInt("Amplifier", effect.getAmplifier());
            list.add(effectTag);
        }
        tag.put(TAG_EFFECTS, list);
        tag.putIntArray(TAG_COLORS, colors);
    }

    private static final class MobEffectUtilCompat {
        private MobEffectUtilCompat() {
        }

        private static Component formatDuration(MobEffectInstance effect) {
            int seconds = Math.max(1, effect.getDuration() / 20);
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return Component.literal(String.format(Locale.ROOT, "%d:%02d", minutes, remainingSeconds));
        }
    }
}
