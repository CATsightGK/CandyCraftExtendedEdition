package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class JellyDungeonRedstoneEvents {
    private JellyDungeonRedstoneEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos leverPos = event.getPos();
        BlockState lever = level.getBlockState(leverPos);
        if (!lever.is(Blocks.LEVER) || !lever.hasProperty(LeverBlock.POWERED) || !isJumpRoomLever(level, leverPos)) {
            return;
        }

        boolean leverWillBePowered = !lever.getValue(LeverBlock.POWERED);
        serverLevel.getServer().execute(() -> syncJumpRoomPistons(serverLevel, leverPos, leverWillBePowered));
    }

    private static boolean isJumpRoomLever(Level level, BlockPos leverPos) {
        BlockState lamp = level.getBlockState(leverPos.north());
        if (!lamp.is(Blocks.REDSTONE_LAMP)) {
            return false;
        }
        BlockPos piston = leverPos.offset(-4, -8, -2);
        BlockPos headPos = piston.relative(Direction.SOUTH);
        return level.getBlockState(piston).is(Blocks.STICKY_PISTON)
            && (level.getBlockState(headPos).isAir()
                || level.getBlockState(headPos).is(CCBlocks.JELLY_SHOCK_ABSORBER.get())
                || level.getBlockState(headPos).is(Blocks.PISTON_HEAD)
                || level.getBlockState(headPos.relative(Direction.SOUTH)).is(CCBlocks.JELLY_SHOCK_ABSORBER.get()));
    }

    private static void syncJumpRoomPistons(ServerLevel level, BlockPos leverPos, boolean leverPowered) {
        level.setBlock(leverPos.north(), Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, leverPowered), 3);
        boolean extended = !leverPowered;
        int baseX = leverPos.getX() - 4;
        int baseY = leverPos.getY() - 11;
        int baseZ = leverPos.getZ() + 40;
        for (int i = 0; i < 6; i++) {
            BlockPos pistonPos = new BlockPos(baseX + i, baseY + 3, baseZ - 42);
            BlockPos headPos = pistonPos.relative(Direction.SOUTH);
            BlockPos jellyOutPos = headPos.relative(Direction.SOUTH);
            level.setBlock(pistonPos, Blocks.STICKY_PISTON.defaultBlockState()
                .setValue(BlockStateProperties.FACING, Direction.SOUTH)
                .setValue(BlockStateProperties.EXTENDED, extended), 3);
            if (extended) {
                level.setBlock(headPos, Blocks.PISTON_HEAD.defaultBlockState()
                    .setValue(PistonHeadBlock.FACING, Direction.SOUTH)
                    .setValue(PistonHeadBlock.TYPE, PistonType.STICKY)
                    .setValue(PistonHeadBlock.SHORT, false), 3);
                level.setBlock(jellyOutPos, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState(), 3);
            } else {
                level.setBlock(headPos, Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(jellyOutPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
