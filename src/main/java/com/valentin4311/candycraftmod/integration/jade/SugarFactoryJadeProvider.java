package com.valentin4311.candycraftmod.integration.jade;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.entity.SugarFactoryBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.Identifiers;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.TooltipPosition;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ProgressArrowElement;

public enum SugarFactoryJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(CandyCraft.MODID, "sugar_factory_info");
    private static final String DATA_KEY = "CandyCraftSugarFactory";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof SugarFactoryBlockEntity factory)) {
            return;
        }

        CompoundTag factoryData = new CompoundTag();
        factoryData.put("Input", factory.getItem(0).copy().save(new CompoundTag()));
        factoryData.put("Expected", factory.getExpectedResult().save(new CompoundTag()));
        factoryData.putInt("Progress", factory.getProgress());
        factoryData.putInt("ProcessTime", factory.getProcessTime());
        data.put(DATA_KEY, factoryData);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        tooltip.remove(Identifiers.UNIVERSAL_ITEM_STORAGE);
        if (!accessor.getServerData().contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag data = accessor.getServerData().getCompound(DATA_KEY);
        ItemStack input = ItemStack.of(data.getCompound("Input"));
        ItemStack expected = ItemStack.of(data.getCompound("Expected"));
        IElementHelper helper = tooltip.getElementHelper();
        if (input.isEmpty() || expected.isEmpty()) {
            return;
        }

        int processTime = Math.max(1, data.getInt("ProcessTime"));
        int progress = Mth.clamp(data.getInt("Progress"), 0, processTime);
        float ratio = progress / (float)processTime;
        IElement inputElement = helper.item(input, 1.0F, Integer.toString(input.getCount())).message(null);
        IElement outputElement = helper.item(expected, 1.0F, Integer.toString(expected.getCount())).message(null);
        tooltip.add(inputElement);
        tooltip.append(helper.spacer(2, 1));
        tooltip.append(new ProgressArrowElement(ratio).translate(new Vec2(0.0F, 1.0F)).message(null));
        tooltip.append(helper.spacer(2, 1));
        tooltip.append(outputElement);
    }

    @Override
    public int getDefaultPriority() {
        return TooltipPosition.TAIL - 100;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
