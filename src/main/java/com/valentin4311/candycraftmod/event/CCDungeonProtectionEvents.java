package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock;
import com.valentin4311.candycraftmod.world.CCDimensions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.PistonEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Keeps dungeon instances pristine: blocks breaking/placing/explosions/pistons
 * around portals and prevents teleport commands from escaping an instance.
 */
@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCDungeonProtectionEvents {
    private CCDungeonProtectionEvents() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (hasDungeonPortalAbove(event.getLevel(), event.getPos())
            || isProtectedDungeonInteraction(event.getLevel(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (CCDimensions.isDungeon(event.getLevel())) {
            event.getAffectedBlocks().clear();
            return;
        }
        event.getAffectedBlocks().removeIf(pos -> hasDungeonPortalAbove(event.getLevel(), pos));
    }

    @SubscribeEvent
    public static void onTeleportCommand(EntityTeleportEvent.TeleportCommand event) {
        if (isProtectedDungeonTeleport(event.getEntity())) {
            event.setCanceled(true);
            ((ServerPlayer) event.getEntity()).displayClientMessage(
                Component.translatable("message.candycraftmod.dungeon.teleport_blocked"), true);
        }
    }

    @SubscribeEvent
    public static void onSpreadPlayersCommand(EntityTeleportEvent.SpreadPlayersCommand event) {
        if (isProtectedDungeonTeleport(event.getEntity())) {
            event.setCanceled(true);
            ((ServerPlayer) event.getEntity()).displayClientMessage(
                Component.translatable("message.candycraftmod.dungeon.teleport_blocked"), true);
        }
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        ServerPlayer player = event.getPlayer();
        if (!CCDimensions.isDungeon(player.level())) {
            return;
        }
        event.setCanceled(true);
        player.displayClientMessage(
            Component.translatable("message.candycraftmod.dungeon.ender_pearl_unwell"), true);
        player.level().playSound(null, event.getPearlEntity().blockPosition(), SoundEvents.GLASS_BREAK,
            SoundSource.PLAYERS, 0.7F, 1.35F);
    }

    @SubscribeEvent
    public static void onPistonMove(PistonEvent.Pre event) {
        net.minecraft.world.level.block.piston.PistonStructureResolver resolver = event.getStructureHelper();
        if (resolver != null && resolver.resolve()
            && (resolver.getToPush().stream().anyMatch(pos -> hasDungeonPortalAbove(event.getLevel(), pos))
                || resolver.getToDestroy().stream().anyMatch(pos -> hasDungeonPortalAbove(event.getLevel(), pos)))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && isProtectedDungeonInteraction(event.getLevel(), player)) {
            event.setCanceled(true);
        }
    }

    private static boolean hasDungeonPortalAbove(LevelAccessor level, BlockPos pos) {
        return DungeonTeleporterBlock.isProtectedSupport(level, pos);
    }

    private static boolean isProtectedDungeonTeleport(Entity entity) {
        return entity instanceof ServerPlayer player
            && !player.isCreative()
            && !player.isSpectator()
            && CCDimensions.isDungeon(player.level());
    }

    /** Also used by interaction rules to guard dungeon blocks against survival players. */
    static boolean isProtectedDungeonInteraction(LevelAccessor level, Player player) {
        if (player == null || player.getAbilities().instabuild) {
            return false;
        }
        if (!(level instanceof Level actualLevel)) {
            return false;
        }
        return CCDimensions.isDungeon(actualLevel);
    }
}
