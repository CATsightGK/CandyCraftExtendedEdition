package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.menu.CandyWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class CandyWorkbenchBlock extends Block {
    private final CandyWorkbenchTheme theme;

    public CandyWorkbenchBlock(CandyWorkbenchTheme theme, BlockBehaviour.Properties properties) {
        super(properties);
        this.theme = theme;
    }

    public CandyWorkbenchTheme theme() {
        return theme;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, menuProvider(level, pos), pos);
        }
        return InteractionResult.CONSUME;
    }

    private MenuProvider menuProvider(Level level, BlockPos pos) {
        Component title = Component.translatable(getDescriptionId());
        return new SimpleMenuProvider((id, inventory, player) ->
            new CandyWorkbenchMenu(id, inventory, ContainerLevelAccess.create(level, pos), theme), title);
    }

    public enum CandyWorkbenchTheme {
        MARSHMALLOW(0xF2A6C8, 0xFFF3FA, 0xB65D89),
        MILK_CHOCOLATE(0x6F3F22, 0xC98952, 0x3B2114),
        WHITE_CHOCOLATE(0xEAD9B6, 0xFFF5D8, 0x9A7B4A),
        DARK_CHOCOLATE(0x2D1710, 0x6B3B28, 0x160A07),
        WHITE_CANDY_CANE(0xF2F2E7, 0xFFFFFF, 0xB8A48E),
        RED_CANDY_CANE(0xC92F38, 0xFFE8E8, 0x801E28),
        GREEN_CANDY_CANE(0x45A845, 0xECFFE8, 0x266C29),
        WHITE_RED_CANDY_CANE(0xE7DDD5, 0xD9454D, 0x8D2530),
        WHITE_GREEN_CANDY_CANE(0xE7EBDD, 0x58B85B, 0x2E742F),
        RED_GREEN_CANDY_CANE(0xC9363E, 0x4CA84F, 0x752327),
        RED_GUMMY(0xF04A48, 0xFF9B9B, 0x8E1E25),
        ORANGE_GUMMY(0xFF8A36, 0xFFC07A, 0x9E481E),
        YELLOW_GUMMY(0xFFD94A, 0xFFF2A0, 0xA87614),
        WHITE_GUMMY(0xFFF1C6, 0xFFFFFF, 0xB69B6A),
        GREEN_GUMMY(0x7CD943, 0xC7FF9A, 0x3E7F20);

        private final int baseColor;
        private final int lightColor;
        private final int darkColor;

        CandyWorkbenchTheme(int baseColor, int lightColor, int darkColor) {
            this.baseColor = baseColor;
            this.lightColor = lightColor;
            this.darkColor = darkColor;
        }

        public int id() {
            return ordinal();
        }

        public int baseColor() {
            return baseColor;
        }

        public int lightColor() {
            return lightColor;
        }

        public int darkColor() {
            return darkColor;
        }

        public static CandyWorkbenchTheme byId(int id) {
            CandyWorkbenchTheme[] values = values();
            return id >= 0 && id < values.length ? values[id] : MARSHMALLOW;
        }
    }
}
