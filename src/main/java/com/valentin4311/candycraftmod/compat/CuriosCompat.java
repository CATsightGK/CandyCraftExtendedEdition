package com.valentin4311.candycraftmod.compat;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.common.util.LazyOptional;

public final class CuriosCompat {
    private static final String CURIOS_MODID = "curios";
    private static final String EMBLEM_SLOT = "candycraft_emblem";
    private static final UUID SLOT_MODIFIER_UUID = UUID.fromString("ef1d6260-10ad-42d1-a81c-85a7ea629061");
    private static final Map<UUID, Integer> syncedEmblemSlots = new HashMap<>();
    private static Method getCuriosInventory;
    private static Method findFirstCurio;
    private static Method addTransientSlotModifier;
    private static Method removeSlotModifier;
    private static Method slotResultStack;
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

    public static void syncCandycraftEmblemSlots(Player player, Item amuletItem) {
        if (!ModList.get().isLoaded(CURIOS_MODID) || player == null || amuletItem == null) {
            return;
        }
        try {
            ensureLookedUp();
            if (getCuriosInventory == null || findFirstCurio == null || addTransientSlotModifier == null || removeSlotModifier == null || slotResultStack == null) {
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
            int currentSlots = getEquippedAmuletCount(inventory, amuletItem);
            int previousSlots = syncedEmblemSlots.getOrDefault(player.getUUID(), -1);
            if (currentSlots == previousSlots && player.tickCount % 20 != 0) {
                return;
            }
            removeSlotModifier.invoke(inventory, EMBLEM_SLOT, SLOT_MODIFIER_UUID);
            if (currentSlots > 0) {
                addTransientSlotModifier.invoke(
                    inventory,
                    EMBLEM_SLOT,
                    SLOT_MODIFIER_UUID,
                    "CandyCraft amulet emblem slots",
                    (double) currentSlots,
                    AttributeModifier.Operation.ADDITION
                );
            }
            syncedEmblemSlots.put(player.getUUID(), currentSlots);
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
    }

    private static int getEquippedAmuletCount(Object inventory, Item amuletItem) throws ReflectiveOperationException {
        Object found = findFirstCurio.invoke(inventory, amuletItem);
        if (!(found instanceof Optional<?> optional) || optional.isEmpty()) {
            return 0;
        }
        Object slotResult = optional.get();
        Object stackObject = slotResultStack.invoke(slotResult);
        if (stackObject instanceof ItemStack stack && stack.is(amuletItem)) {
            return Math.max(0, stack.getCount());
        }
        return 0;
    }

    private static void ensureLookedUp() throws ReflectiveOperationException {
        if (lookedUp) {
            return;
        }
        lookedUp = true;
        Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
        Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
        Class<?> slotResult = Class.forName("top.theillusivec4.curios.api.SlotResult");
        getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
        findFirstCurio = handler.getMethod("findFirstCurio", Item.class);
        addTransientSlotModifier = handler.getMethod("addTransientSlotModifier", String.class, UUID.class, String.class, double.class, AttributeModifier.Operation.class);
        removeSlotModifier = handler.getMethod("removeSlotModifier", String.class, UUID.class);
        slotResultStack = slotResult.getMethod("stack");
    }
}
