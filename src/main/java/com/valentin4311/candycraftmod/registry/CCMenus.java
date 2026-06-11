package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.SugarFactoryBlock;
import com.valentin4311.candycraftmod.menu.LicoriceFurnaceMenu;
import com.valentin4311.candycraftmod.menu.SugarFactoryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, CandyCraft.MODID);

    public static final RegistryObject<MenuType<SugarFactoryMenu>> SUGAR_FACTORY =
        MENU_TYPES.register("sugar_factory", () -> IForgeMenuType.create((id, inventory, data) -> {
            boolean advanced = false;
            if (data != null) {
                BlockPos pos = data.readBlockPos();
                BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
                if (blockEntity != null && blockEntity.getBlockState().getBlock() instanceof SugarFactoryBlock sugarFactoryBlock) {
                    advanced = sugarFactoryBlock.isAdvanced();
                }
            }
            return new SugarFactoryMenu(id, inventory, advanced);
        }));

    public static final RegistryObject<MenuType<LicoriceFurnaceMenu>> LICORICE_FURNACE =
        MENU_TYPES.register("licorice_furnace", () -> IForgeMenuType.create((id, inventory, data) ->
            new LicoriceFurnaceMenu(id, inventory)
        ));

    private CCMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
