package com.valentin4311.candycraftmod.integration.jade;

import com.valentin4311.candycraftmod.block.AlchemyTableBlock;
import com.valentin4311.candycraftmod.block.SugarFactoryBlock;
import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import com.valentin4311.candycraftmod.block.entity.SugarFactoryBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class CandyCraftJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(AlchemyTableJadeProvider.INSTANCE, AlchemyTableBlockEntity.class);
        registration.registerFluidStorage(AlchemyTableFluidJadeProvider.INSTANCE, AlchemyTableBlockEntity.class);
        registration.registerBlockDataProvider(SugarFactoryJadeProvider.INSTANCE, SugarFactoryBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AlchemyTableJadeProvider.INSTANCE, AlchemyTableBlock.class);
        registration.registerFluidStorageClient(AlchemyTableFluidJadeProvider.INSTANCE);
        registration.registerBlockComponent(SugarFactoryJadeProvider.INSTANCE, SugarFactoryBlock.class);
    }
}
