package com.valentin4311.candycraftmod.integration.jade;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.entity.AlchemyLiquidKind;
import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import com.valentin4311.candycraftmod.registry.CCFluids;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

public enum AlchemyTableFluidJadeProvider
        implements IServerExtensionProvider<AlchemyTableBlockEntity, CompoundTag>,
        IClientExtensionProvider<CompoundTag, FluidView> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(CandyCraft.MODID, "alchemy_table_fluid");

    @Override
    public List<ViewGroup<CompoundTag>> getGroups(ServerPlayer player, ServerLevel level,
            AlchemyTableBlockEntity table, boolean showDetails) {
        CompoundTag view = new CompoundTag();
        view.putString("Kind", table.getLiquidKind().id());
        view.putInt("Amount", table.getDisplayedSyrupUnits());
        view.putInt("Capacity", AlchemyTableBlockEntity.MAX_LIQUID_UNITS);
        return List.of(new ViewGroup<>(List.of(view)));
    }

    @Override
    public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor,
            List<ViewGroup<CompoundTag>> groups) {
        return ClientViewGroup.map(groups, this::readFluidView, null);
    }

    private FluidView readFluidView(CompoundTag data) {
        AlchemyLiquidKind kind = AlchemyLiquidKind.byId(data.getString("Kind"));
        int capacity = Math.max(1, data.getInt("Capacity"));
        int amount = Mth.clamp(data.getInt("Amount"), 0, capacity);
        Component fluidName = Component.translatable("jade.candycraftmod.alchemy.liquid." + kind.id());

        FluidView view = new FluidView(fluidOverlay(kind, amount));
        view.fluidName = fluidName;
        view.current = Integer.toString(amount);
        view.max = Integer.toString(capacity);
        view.ratio = amount / (float)capacity;
        view.overrideText = Component.translatable("jade.candycraftmod.alchemy.fluid_bar",
            fluidName, amount, capacity);
        return view;
    }

    private static IElement fluidOverlay(AlchemyLiquidKind kind, int amount) {
        IElementHelper helper = IElementHelper.get();
        if (kind == AlchemyLiquidKind.NONE) {
            return helper.spacer(16, 16);
        }
        if (kind == AlchemyLiquidKind.MILK) {
            return new AlchemyFluidSpriteElement(
                new ResourceLocation("minecraft", "block/quartz_block_bottom"));
        }

        Fluid fluid = switch (kind) {
            case GRENADINE -> CCFluids.SOURCE_GRENADINE.get();
            case WATER -> Fluids.WATER;
            case CHOCOLATE -> CCFluids.SOURCE_LIQUID_CHOCOLATE.get();
            case LIQUID_CANDY -> CCFluids.SOURCE_LIQUID_CANDY.get();
            case LAVA -> Fluids.LAVA;
            case CARAMEL -> CCFluids.SOURCE_CARAMEL.get();
            case NONE, MILK -> Fluids.EMPTY;
        };
        long fluidAmount = (long)Math.max(1, amount) * JadeFluidObject.bucketVolume();
        return helper.fluid(JadeFluidObject.of(fluid, fluidAmount));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
