package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCToolProperties;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCTooltipEvents {
    private static final DecimalFormat ATTRIBUTE_FORMAT = new DecimalFormat("#.##");

    private CCTooltipEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        CCToolProperties.Profile profile = CCToolProperties.get(event.getItemStack());
        Double attackSpeed = profile == null ? null : profile.attackSpeed();
        if (attackSpeed == null) {
            return;
        }

        String attributeName = Component.translatable(Attributes.ATTACK_SPEED.getDescriptionId()).getString();
        List<Component> lines = event.getToolTip();
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).getString().contains(attributeName)) {
                Component value = Component.translatable(
                    "attribute.modifier.equals.0",
                    ATTRIBUTE_FORMAT.format(attackSpeed),
                    Component.translatable(Attributes.ATTACK_SPEED.getDescriptionId())
                ).withStyle(ChatFormatting.DARK_GREEN);
                lines.set(index, Component.literal(" ").append(value));
                return;
            }
        }
    }
}
