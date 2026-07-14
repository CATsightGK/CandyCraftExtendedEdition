package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class EmblemBasketButton extends Button {
    private static final ItemStack MAGIC_CANDY = new ItemStack(CCItems.TELEPORTER.get());

    public EmblemBasketButton(int x, int y, OnPress onPress) {
        super(x, y, 18, 18, Component.translatable("button.candycraftmod.emblem_basket"), onPress, DEFAULT_NARRATION);
        setTooltip(Tooltip.create(Component.translatable("button.candycraftmod.emblem_basket")));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int border = isHoveredOrFocused() ? 0xFFFFD1E5 : 0xFF23101D;
        int fill = isHoveredOrFocused() ? 0xFFE77BB0 : 0xFFB64F83;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, border);
        graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, fill);
        graphics.renderItem(MAGIC_CANDY, getX() + 1, getY() + 1);
    }
}
