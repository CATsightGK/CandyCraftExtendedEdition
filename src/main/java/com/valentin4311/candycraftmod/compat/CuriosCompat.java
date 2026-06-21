package com.valentin4311.candycraftmod.compat;

import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.common.util.LazyOptional;

public final class CuriosCompat {
    private static final String CURIOS_MODID = "curios";
    private static Method getCuriosInventory;
    private static Method findFirstCurio;
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

    private static void ensureLookedUp() throws ReflectiveOperationException {
        if (lookedUp) {
            return;
        }
        lookedUp = true;
        Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
        Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
        getCuriosInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
        findFirstCurio = handler.getMethod("findFirstCurio", Item.class);
    }
}
