package com.valentin4311.candycraftmod.integration.jade;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum AlchemyTableJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(CandyCraft.MODID, "alchemy_table_info");
    private static final String DATA_KEY = "CandyCraftAlchemy";
    private static final String POWER_SOURCES_KEY = "PowerSources";
    private static final String MIXER_SPEED_KEY = "MixerSpeed";

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof AlchemyTableBlockEntity table)) {
            return;
        }

        Set<ResourceLocation> sourceIds = new LinkedHashSet<>();
        for (Direction direction : Direction.values()) {
            BlockState state = accessor.getLevel().getBlockState(accessor.getPosition().relative(direction));
            if (!AlchemyTableBlockEntity.isMixerPowerSource(state)) {
                continue;
            }
            Block block = state.is(CCBlocks.LICORICE_FURNACE_ON.get())
                ? CCBlocks.LICORICE_FURNACE.get()
                : state.getBlock();
            sourceIds.add(BuiltInRegistries.BLOCK.getKey(block));
        }

        ListTag sources = new ListTag();
        sourceIds.forEach(id -> sources.add(StringTag.valueOf(id.toString())));
        CompoundTag alchemyData = new CompoundTag();
        alchemyData.put(POWER_SOURCES_KEY, sources);
        alchemyData.putInt(MIXER_SPEED_KEY, Math.round(table.getTargetMixerSpeed()));
        data.put(DATA_KEY, alchemyData);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!accessor.getServerData().contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            return;
        }

        CompoundTag data = accessor.getServerData().getCompound(DATA_KEY);
        ListTag sourceIds = data.getList(POWER_SOURCES_KEY, Tag.TAG_STRING);
        MutableComponent sources = Component.empty();
        for (int i = 0; i < sourceIds.size(); ++i) {
            ResourceLocation id = ResourceLocation.tryParse(sourceIds.getString(i));
            if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                continue;
            }
            if (!sources.getSiblings().isEmpty()) {
                sources.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
            sources.append(Component.translatable(BuiltInRegistries.BLOCK.get(id).getDescriptionId())
                .withStyle(ChatFormatting.WHITE));
        }
        if (sources.getSiblings().isEmpty()) {
            sources.append(Component.translatable("jade.candycraftmod.alchemy.no_power")
                .withStyle(ChatFormatting.DARK_GRAY));
        }

        tooltip.add(Component.translatable("jade.candycraftmod.alchemy.power_sources", sources)
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("jade.candycraftmod.alchemy.mixer_speed",
            Component.literal(Integer.toString(data.getInt(MIXER_SPEED_KEY))).withStyle(ChatFormatting.AQUA))
            .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
