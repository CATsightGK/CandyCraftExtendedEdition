package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.entity.DragonEggBlockEntity;
import com.valentin4311.candycraftmod.block.entity.LicoriceFurnaceBlockEntity;
import com.valentin4311.candycraftmod.block.entity.MarshmallowChestBlockEntity;
import com.valentin4311.candycraftmod.block.entity.SugarFactoryBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CandyCraft.MODID);

    public static final RegistryObject<BlockEntityType<SugarFactoryBlockEntity>> SUGAR_FACTORY =
        BLOCK_ENTITY_TYPES.register("sugar_factory", () -> new BlockEntityType<>(
            SugarFactoryBlockEntity::new,
            java.util.Set.of(CCBlocks.SUGAR_FACTORY.get(), CCBlocks.ADVANCED_SUGAR_FACTORY.get()),
            null
        ));

    public static final RegistryObject<BlockEntityType<LicoriceFurnaceBlockEntity>> LICORICE_FURNACE =
        BLOCK_ENTITY_TYPES.register("licorice_furnace", () -> new BlockEntityType<>(
            LicoriceFurnaceBlockEntity::new,
            java.util.Set.of(CCBlocks.LICORICE_FURNACE.get(), CCBlocks.LICORICE_FURNACE_ON.get()),
            null
        ));

    public static final RegistryObject<BlockEntityType<AlchemyTableBlockEntity>> ALCHEMY_TABLE =
        BLOCK_ENTITY_TYPES.register("alchemy_table", () -> new BlockEntityType<>(
            AlchemyTableBlockEntity::new,
            java.util.Set.of(CCBlocks.ALCHEMY_TABLE.get()),
            null
        ));

    public static final RegistryObject<BlockEntityType<DragonEggBlockEntity>> DRAGON_EGG =
        BLOCK_ENTITY_TYPES.register("dragon_egg", () -> new BlockEntityType<>(
            DragonEggBlockEntity::new,
            java.util.Set.of(CCBlocks.DRAGON_EGG_BLOCK.get(), CCBlocks.BEETLE_EGG_BLOCK.get()),
            null
        ));

    public static final RegistryObject<BlockEntityType<MarshmallowChestBlockEntity>> MARSHMALLOW_CHEST =
        BLOCK_ENTITY_TYPES.register("marshmallow_chest", () -> new BlockEntityType<>(
            MarshmallowChestBlockEntity::new,
            java.util.Set.of(CCBlocks.MARSHMALLOW_CHEST.get(), CCBlocks.MARSHMALLOW_CHEST_DARK.get(), CCBlocks.MARSHMALLOW_CHEST_LIGHT.get()),
            null
        ));

    private CCBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
