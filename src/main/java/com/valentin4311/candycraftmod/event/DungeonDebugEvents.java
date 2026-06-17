package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.world.feature.JellyDungeonFeature;
import com.valentin4311.candycraftmod.world.feature.SuguardDungeonFeature;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DungeonDebugEvents {
    private DungeonDebugEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("candycraft_debug_dungeons")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                BlockPos base = BlockPos.containing(context.getSource().getPosition()).offset(0, 1, 0);
                JellyDungeonFeature.generateDebugShowcase(level, base);
                SuguardDungeonFeature.generateDebugShowcase(level, base.offset(240, 0, 0));
                context.getSource().sendSuccess(() -> Component.literal(
                    "Generated CandyCraft dungeon debug showcase at " + base.toShortString()
                        + " and " + base.offset(240, 0, 0).toShortString()), true);
                return 1;
            }));
    }
}
