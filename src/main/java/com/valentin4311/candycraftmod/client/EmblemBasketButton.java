package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import java.util.function.IntSupplier;

public final class EmblemBasketButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        CandyCraft.MODID,
        "textures/gui/emblem_basket_button.png"
    );
    private final IntSupplier xSupplier;
    private final IntSupplier ySupplier;

    public EmblemBasketButton(IntSupplier xSupplier, IntSupplier ySupplier, OnPress onPress) {
        super(xSupplier.getAsInt(), ySupplier.getAsInt(), 20, 18,
            Component.translatable("button.candycraftmod.emblem_basket"), onPress, DEFAULT_NARRATION);
        this.xSupplier = xSupplier;
        this.ySupplier = ySupplier;
        setTooltip(Tooltip.create(Component.translatable("button.candycraftmod.emblem_basket")));
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        setX(xSupplier.getAsInt());
        setY(ySupplier.getAsInt());
        graphics.blit(TEXTURE, getX() + 2, getY() + 1, 0, 0, 16, 16, 16, 16);
    }
}
