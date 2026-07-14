package com.valentin4311.candycraftmod.compat;

import com.valentin4311.candycraftmod.inventory.EmblemBasketContainer;
import com.valentin4311.candycraftmod.item.EmblemItem;
import com.valentin4311.candycraftmod.menu.EmblemBasketMenu;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

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

    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MODID);
    }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        if (!isLoaded() || entity == null || item == null) {
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
        if (!isLoaded() || player == null) {
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
            int currentSlots = normalizeCuriosEmblems(player, inventory);
            int maxSlots = EmblemItem.getRegisteredCount();
            int targetSlots = Math.min(maxSlots, currentSlots + 1);
            int modifierAmount = Math.max(0, targetSlots - 1);
            int previousAmount = syncedEmblemSlots.getOrDefault(player.getUUID(), -1);
            if (modifierAmount == previousAmount) {
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

    public static void clearPlayerCache(Player player) {
        if (player != null) {
            syncedEmblemSlots.remove(player.getUUID());
        }
    }

    private static int normalizeCuriosEmblems(Player player, Object inventory) throws ReflectiveOperationException {
        Object found = getStacksHandler.invoke(inventory, EMBLEM_SLOT);
        if (!(found instanceof Optional<?> optional) || optional.isEmpty()) {
            return 0;
        }
        Object stackHandler = getStacks.invoke(optional.get());
        if (!(stackHandler instanceof IItemHandler itemHandler)) {
            return 0;
        }
        Set<Item> seen = new HashSet<>();
        List<ItemStack> unique = new ArrayList<>();
        List<ItemStack> duplicates = new ArrayList<>();
        List<Integer> duplicateSlots = new ArrayList<>();
        boolean needsCompaction = false;
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty() && EmblemBasketMenu.isEmblem(stack)) {
                if (seen.add(stack.getItem())) {
                    if (slot != unique.size()) {
                        needsCompaction = true;
                    }
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    unique.add(copy);
                } else {
                    ItemStack copy = stack.copy();
                    copy.setCount(1);
                    duplicates.add(copy);
                    duplicateSlots.add(slot);
                    needsCompaction = true;
                }
            }
        }

        if (needsCompaction && itemHandler instanceof IItemHandlerModifiable modifiable) {
            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                modifiable.setStackInSlot(slot, ItemStack.EMPTY);
            }
            for (int slot = 0; slot < unique.size(); slot++) {
                modifiable.setStackInSlot(slot, unique.get(slot));
            }
        } else if (!duplicateSlots.isEmpty()) {
            duplicates.clear();
            for (int index = duplicateSlots.size() - 1; index >= 0; index--) {
                int slot = duplicateSlots.get(index);
                ItemStack extracted = itemHandler.extractItem(slot, itemHandler.getStackInSlot(slot).getCount(), false);
                if (!extracted.isEmpty()) {
                    extracted.setCount(1);
                    duplicates.add(extracted);
                }
            }
        }
        for (ItemStack duplicate : duplicates) {
            player.getInventory().placeItemBackInInventory(duplicate);
        }
        return unique.size();
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
