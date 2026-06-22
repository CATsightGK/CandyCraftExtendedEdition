package com.valentin4311.candycraftmod.compat;

import com.valentin4311.candycraftmod.inventory.EmblemBasketContainer;
import com.valentin4311.candycraftmod.menu.EmblemBasketMenu;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public final class CuriosCompat {
    private static final String CURIOS_MODID = "curios";
    private static final String EMBLEM_SLOT = "candycraft_emblem";
    private static final UUID SLOT_MODIFIER_UUID = UUID.fromString("ef1d6260-10ad-42d1-a81c-85a7ea629061");
    private static final Map<UUID, Integer> syncedEmblemSlots = new HashMap<>();
    private static Method getCuriosInventory;
    private static Method findFirstCurio;
    private static Method getStacksHandler;
    private static Method addTransientSlotModifier;
    private static Method removeSlotModifier;
    private static Method getStacks;
    private static boolean lookedUp;

    private CuriosCompat() {
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        if (!ModList.get().isLoaded(CURIOS_MODID) || entity == null || item == null) {
            return false;
        }
        try {
            ensureLookedUp();
            if (getCuriosInventory == null || findFirstCurio == null) {
                return false;
            }
            Object result = getCuriosInventory.invoke(null, entity);
            if (!(result instanceof LazyOptional<?> optional)) {
                return false;
            }
            Optional<?> handler = optional.resolve();
            if (handler.isEmpty()) {
                return false;
            }
            Object found = findFirstCurio.invoke(handler.get(), item);
            return found instanceof Optional<?> curio && curio.isPresent();
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    public static void syncEmblemSlots(Player player) {
        if (!ModList.get().isLoaded(CURIOS_MODID) || player == null) {
            return;
        }
        try {
            ensureLookedUp();
            if (getCuriosInventory == null || getStacksHandler == null || addTransientSlotModifier == null || removeSlotModifier == null || getStacks == null) {
                return;
            }
            Object result = getCuriosInventory.invoke(null, player);
            if (!(result instanceof LazyOptional<?> optional)) {
                return;
            }
            Optional<?> handler = optional.resolve();
            if (handler.isEmpty()) {
                return;
            }
            Object inventory = handler.get();
            int currentSlots = countCuriosEmblems(inventory);
            int targetSlots = Math.min(EmblemBasketContainer.MAX_EMBLEMS, currentSlots + 1);
            int modifierAmount = Math.max(0, targetSlots - 1);
            int previousAmount = syncedEmblemSlots.getOrDefault(player.getUUID(), -1);
            if (modifierAmount == previousAmount && player.tickCount % 20 != 0) {
                return;
            }
            removeSlotModifier.invoke(inventory, EMBLEM_SLOT, SLOT_MODIFIER_UUID);
            if (modifierAmount > 0) {
                addTransientSlotModifier.invoke(
                    inventory,
                    EMBLEM_SLOT,
                    SLOT_MODIFIER_UUID,
                    "CandyCraft emblem basket slots",
                    (double) modifierAmount,
                    AttributeModifier.Operation.ADDITION
                );
            }
            syncedEmblemSlots.put(player.getUUID(), modifierAmount);
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }

    private static int countCuriosEmblems(Object inventory) throws ReflectiveOperationException {
        Object found = getStacksHandler.invoke(inventory, EMBLEM_SLOT);
        if (!(found instanceof Optional<?> optional) || optional.isEmpty()) {
            return 0;
        }
        Object stackHandler = getStacks.invoke(optional.get());
        if (!(stackHandler instanceof IItemHandler itemHandler)) {
            return 0;
        }
        int occupied = 0;
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && EmblemBasketMenu.isEmblem(stack)) {
                occupied++;
            }
        }
        return occupied;
    }

    private static void ensureLookedUp() throws ReflectiveOperationException {
        if (lookedUp) {
            return;
        }
        lookedUp = true;
        Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
        Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
        Class<?> stacksHandler = Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler");
        getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
        findFirstCurio = handler.getMethod("findFirstCurio", Item.class);
        getStacksHandler = handler.getMethod("getStacksHandler", String.class);
        addTransientSlotModifier = handler.getMethod("addTransientSlotModifier", String.class, UUID.class, String.class, double.class, AttributeModifier.Operation.class);
        removeSlotModifier = handler.getMethod("removeSlotModifier", String.class, UUID.class);
        getStacks = stacksHandler.getMethod("getStacks");
    }
}
