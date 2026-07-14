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
    private static final ResourceLocation EMPTY_EMBLEM_SLOT = new ResourceLocation(
        CandyCraft.MODID,
        "textures/slot/candycraft_emblem.png"
    );
    private static final int BORDER = 0xFF351326;
    private static final int DARK_PINK = 0xFF9E3F70;
    private static final int MID_PINK = 0xFFE276AA;
    private static final int LIGHT_PINK = 0xFFFFC8DF;
    private static final int HIGHLIGHT = 0xFFFFE4EF;

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
        graphics.fill(left, top, left + imageWidth, top + imageHeight, BORDER);
        graphics.fill(left + 2, top + 2, left + imageWidth - 2, top + imageHeight - 2, DARK_PINK);
        graphics.fill(left + 4, top + 4, left + imageWidth - 4, top + imageHeight - 4, MID_PINK);
        graphics.fill(left + 5, top + 17, left + imageWidth - 5, top + menu.getInventoryStartY() - 5, LIGHT_PINK);
        graphics.fill(left + 5, top + menu.getInventoryStartY() - 7,
            left + imageWidth - 5, top + imageHeight - 5, 0xFFD9669B);
        graphics.fill(left + 5, top + 17, left + imageWidth - 5, top + 19, HIGHLIGHT);

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
        graphics.fill(x, y, x + 18, y + 18, BORDER);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF6E2B52);
        graphics.fill(x + 2, y + 2, x + 16, y + 16, 0x66FFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
