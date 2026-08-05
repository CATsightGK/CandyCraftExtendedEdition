package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.menu.EmblemBasketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class EmblemBasketScreen extends AbstractContainerScreen<EmblemBasketMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
        CandyCraft.MODID,
        "textures/gui/container/emblem_basket.png"
    );
    private static final ResourceLocation MARSHMALLOW_SLOT = new ResourceLocation(
        CandyCraft.MODID,
        "textures/gui/marshmallow_slot.png"
    );
    private static final ResourceLocation EMPTY_EMBLEM_SLOT = new ResourceLocation(
        CandyCraft.MODID,
        "textures/slot/candycraft_emblem.png"
    );

    public EmblemBasketScreen(EmblemBasketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = EmblemBasketMenu.IMAGE_WIDTH;
        imageHeight = menu.getImageHeight();
        inventoryLabelY = menu.getInventoryStartY() - 11;
        titleLabelY = 7;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;
        graphics.blit(BACKGROUND, left, top, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        int visibleEmblems = menu.getVisibleEmblemSlots();
        for (int slotIndex = 0; slotIndex < menu.slots.size(); slotIndex++) {
            Slot slot = menu.getSlot(slotIndex);
            if (slotIndex < menu.getEmblemSlotCount() && slotIndex >= visibleEmblems) {
                continue;
            }
            drawSlot(graphics, left + slot.x - 1, top + slot.y - 1);
            if (slotIndex < menu.getEmblemSlotCount() && !slot.hasItem()) {
                graphics.blit(EMPTY_EMBLEM_SLOT, left + slot.x, top + slot.y, 0, 0, 16, 16, 16, 16);
            }
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blit(MARSHMALLOW_SLOT, x, y, 0, 0, 18, 18, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x51343E, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x51343E, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
