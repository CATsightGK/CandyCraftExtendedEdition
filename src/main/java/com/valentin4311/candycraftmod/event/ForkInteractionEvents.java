package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.item.ForkItem;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForkInteractionEvents {
    private static final String BLOCK_ENTITY_ARMED = CandyCraft.MODID + ".fork_block_entity_armed";
    private static final String BLOCK_ENTITY_POS = CandyCraft.MODID + ".fork_block_entity_pos";
    private static final String BLOCK_ENTITY_DIMENSION = CandyCraft.MODID + ".fork_block_entity_dimension";
    private static final String BLOCK_ENTITY_EXPIRES = CandyCraft.MODID + ".fork_block_entity_expires";
    private static final int BLOCK_ENTITY_ARM_TICKS = 30 * 20;

    private ForkInteractionEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(CCItems.FORK.get())) {
            return;
        }
        Player player = event.getEntity();
        BlockState state = event.getLevel().getBlockState(event.getPos());
        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity != null) {
            if (!player.isShiftKeyDown()) {
                clearBlockEntityTarget(player, event.getLevel());
                return;
            }

            if (isArmedFor(player, event.getLevel(), event.getPos().asLong())) {
                clearBlockEntityTarget(player, event.getLevel());
                if (ForkItem.beginForkingBlock(
                        stack,
                        event.getLevel(),
                        event.getPos(),
                        player,
                        event.getHand(),
                        state)) {
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
                }
                return;
            }

            armBlockEntityTarget(player, event.getLevel(), event.getPos().asLong());
            event.setUseBlock(Event.Result.ALLOW);
            event.setUseItem(Event.Result.DENY);
            return;
        }

        clearBlockEntityTarget(player, event.getLevel());
        if (!(state.getBlock() instanceof DoorBlock) && !(state.getBlock() instanceof TrapDoorBlock)) {
            return;
        }
        if (!ForkItem.beginForkingBlock(
                stack,
                event.getLevel(),
                event.getPos(),
                player,
                event.getHand(),
                state)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
    }

    private static void armBlockEntityTarget(Player player, Level level, long pos) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(sideKey(BLOCK_ENTITY_ARMED, level), true);
        data.putLong(sideKey(BLOCK_ENTITY_POS, level), pos);
        data.putString(sideKey(BLOCK_ENTITY_DIMENSION, level), level.dimension().location().toString());
        data.putLong(sideKey(BLOCK_ENTITY_EXPIRES, level), level.getGameTime() + BLOCK_ENTITY_ARM_TICKS);
    }

    private static boolean isArmedFor(Player player, Level level, long pos) {
        CompoundTag data = player.getPersistentData();
        ResourceLocation dimension = ResourceLocation.tryParse(data.getString(sideKey(BLOCK_ENTITY_DIMENSION, level)));
        return data.getBoolean(sideKey(BLOCK_ENTITY_ARMED, level))
            && data.getLong(sideKey(BLOCK_ENTITY_POS, level)) == pos
            && level.getGameTime() <= data.getLong(sideKey(BLOCK_ENTITY_EXPIRES, level))
            && level.dimension().location().equals(dimension);
    }

    private static void clearBlockEntityTarget(Player player, Level level) {
        CompoundTag data = player.getPersistentData();
        data.remove(sideKey(BLOCK_ENTITY_ARMED, level));
        data.remove(sideKey(BLOCK_ENTITY_POS, level));
        data.remove(sideKey(BLOCK_ENTITY_DIMENSION, level));
        data.remove(sideKey(BLOCK_ENTITY_EXPIRES, level));
    }

    private static String sideKey(String key, Level level) {
        return key + (level.isClientSide ? ".client" : ".server");
    }
}
