package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.SugarFactoryBlock;
import com.valentin4311.candycraftmod.block.CandyWorkbenchBlock;
import com.valentin4311.candycraftmod.menu.CandyWorkbenchMenu;
import com.valentin4311.candycraftmod.menu.EmblemBasketMenu;
import com.valentin4311.candycraftmod.menu.LicoriceFurnaceMenu;
import com.valentin4311.candycraftmod.menu.SugarFactoryMenu;
import com.valentin4311.candycraftmod.menu.MarshmallowChestMenu;
import com.valentin4311.candycraftmod.block.MarshmallowChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.ContainerLevelAccess;
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

    public static final RegistryObject<MenuType<EmblemBasketMenu>> EMBLEM_BASKET =
        MENU_TYPES.register("emblem_basket", () -> IForgeMenuType.create((id, inventory, data) ->
            new EmblemBasketMenu(id, inventory)
        ));

    public static final RegistryObject<MenuType<CandyWorkbenchMenu>> CANDY_WORKBENCH =
        MENU_TYPES.register("candy_workbench", () -> IForgeMenuType.create((id, inventory, data) -> {
            CandyWorkbenchBlock.CandyWorkbenchTheme theme = CandyWorkbenchBlock.CandyWorkbenchTheme.MARSHMALLOW;
            ContainerLevelAccess access = ContainerLevelAccess.NULL;
            if (data != null) {
                BlockPos pos = data.readBlockPos();
                access = ContainerLevelAccess.create(inventory.player.level(), pos);
                if (inventory.player.level().getBlockState(pos).getBlock() instanceof CandyWorkbenchBlock workbench) {
                    theme = workbench.theme();
                }
            }
            return new CandyWorkbenchMenu(id, inventory, access, theme);
        }));

    public static final RegistryObject<MenuType<MarshmallowChestMenu>> MARSHMALLOW_CHEST =
        MENU_TYPES.register("marshmallow_chest", () -> IForgeMenuType.create((id, inventory, data) -> {
            MarshmallowChestBlock.Theme theme = MarshmallowChestBlock.Theme.NORMAL;
            if (data != null) {
                BlockPos pos = data.readBlockPos();
                if (inventory.player.level().getBlockState(pos).getBlock() instanceof MarshmallowChestBlock chest) {
                    theme = chest.theme();
                }
            }
            return new MarshmallowChestMenu(id, inventory, theme);
        }));

    private CCMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
