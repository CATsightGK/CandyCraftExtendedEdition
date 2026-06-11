package com.valentin4311.candycraftmod.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class LegacyLeavesBlock extends Block {
    public static final BooleanProperty CHECK_DECAY = BooleanProperty.create("check_decay");
    public static final BooleanProperty DECAYABLE = BooleanProperty.create("decayable");
    public static final EnumProperty<LeafVariant> VARIANT = EnumProperty.create("variant", LeafVariant.class);

    public LegacyLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(CHECK_DECAY, false)
            .setValue(DECAYABLE, true)
            .setValue(VARIANT, LeafVariant.OAK));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHECK_DECAY, DECAYABLE, VARIANT);
    }

    public enum LeafVariant implements StringRepresentable {
        OAK("oak"),
        SPRUCE("spruce"),
        BIRCH("birch"),
        JUNGLE("jungle");

        private final String serializedName;

        LeafVariant(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}
