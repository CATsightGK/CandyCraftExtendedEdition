package com.valentin4311.candycraftmod.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.world.feature.JellyDungeonFeature;
import com.valentin4311.candycraftmod.world.feature.SuguardDungeonFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.LevelResource;
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
        event.getDispatcher().register(Commands.literal("candycraft_debug_suguard_dungeon")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                BlockPos base = BlockPos.containing(context.getSource().getPosition()).offset(0, 1, 0);
                SuguardDungeonFeature.generateDebugShowcase(level, base);
                context.getSource().sendSuccess(() -> Component.literal(
                    "Generated Suguard dungeon debug showcase at " + base.toShortString()), true);
                return 1;
            }));
        event.getDispatcher().register(Commands.literal("candycraft_debug_jelly_water_room")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                BlockPos base = BlockPos.containing(context.getSource().getPosition()).offset(0, 1, 0);
                JellyDungeonFeature.generateDebugWaterRoom(level, base);
                context.getSource().sendSuccess(() -> Component.literal(
                    "Generated CandyCraft jelly water room at " + base.toShortString()), true);
                return 1;
            }));
        event.getDispatcher().register(Commands.literal("candycraft_export_suguard_room")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("room", StringArgumentType.word())
                .then(Commands.argument("origin", BlockPosArgument.blockPos())
                    .executes(context -> {
                        ServerLevel level = context.getSource().getLevel();
                        String room = StringArgumentType.getString(context, "room").toLowerCase(Locale.ROOT);
                        BlockPos origin = BlockPosArgument.getLoadedBlockPos(context, "origin");
                        RoomBounds bounds = suguardRoomBounds(room);
                        if (bounds == null) {
                            context.getSource().sendFailure(Component.literal(
                                "Unknown room. Use spawn, z_corridor, x_corridor, archer, water, barrier, jump, fall, fight, boss."));
                            return 0;
                        }
                        try {
                            Path exported = exportRoom(level, room, origin, bounds);
                            context.getSource().sendSuccess(() -> Component.literal(
                                "Exported " + room + " to " + exported), true);
                            return 1;
                        } catch (IOException e) {
                            context.getSource().sendFailure(Component.literal("Failed to export room: " + e.getMessage()));
                            return 0;
                        }
                    }))));
    }

    private static RoomBounds suguardRoomBounds(String room) {
        return switch (room) {
            case "spawn" -> new RoomBounds(-4, 0, -4, 10, 6, 4);
            case "z_corridor" -> new RoomBounds(-1, -1, -8, 3, 4, 0);
            case "x_corridor" -> new RoomBounds(-8, -1, -1, 0, 4, 3);
            case "archer" -> new RoomBounds(-10, -20, -50, 10, 10, 0);
            case "water" -> new RoomBounds(-5, -2, -30, 5, 5, 0);
            case "barrier" -> new RoomBounds(-11, -18, 0, 11, 10, 53);
            case "jump" -> new RoomBounds(-4, -54, 0, 4, 187, 19);
            case "fall" -> new RoomBounds(-15, -54, -4, 0, 186, 4);
            case "fight" -> new RoomBounds(-40, -10, -21, 1, 60, 21);
            case "boss" -> new RoomBounds(-21, -2, -21, 21, 36, 21);
            default -> null;
        };
    }

    private static Path exportRoom(ServerLevel level, String room, BlockPos origin, RoomBounds bounds) throws IOException {
        CompoundTag root = new CompoundTag();
        root.putString("type", "candycraft_suguard_room_export");
        root.putString("room", room);
        root.putString("dimension", level.dimension().location().toString());
        root.put("origin", NbtUtils.writeBlockPos(origin));
        root.put("min", NbtUtils.writeBlockPos(new BlockPos(bounds.minX, bounds.minY, bounds.minZ)));
        root.put("max", NbtUtils.writeBlockPos(new BlockPos(bounds.maxX, bounds.maxY, bounds.maxZ)));
        root.put("size", NbtUtils.writeBlockPos(new BlockPos(
            bounds.maxX - bounds.minX + 1,
            bounds.maxY - bounds.minY + 1,
            bounds.maxZ - bounds.minZ + 1
        )));

        ListTag blocks = new ListTag();
        for (int x = bounds.minX; x <= bounds.maxX; x++) {
            for (int y = bounds.minY; y <= bounds.maxY; y++) {
                for (int z = bounds.minZ; z <= bounds.maxZ; z++) {
                    BlockPos worldPos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(worldPos);
                    if (state.isAir()) {
                        continue;
                    }
                    CompoundTag entry = new CompoundTag();
                    entry.putInt("x", x);
                    entry.putInt("y", y);
                    entry.putInt("z", z);
                    entry.putString("state", stateString(state));
                    BlockEntity blockEntity = level.getBlockEntity(worldPos);
                    if (blockEntity != null) {
                        entry.put("blockEntity", blockEntity.saveWithFullMetadata());
                    }
                    blocks.add(entry);
                }
            }
        }
        root.put("blocks", blocks);

        Path directory = level.getServer().getWorldPath(LevelResource.ROOT).resolve("candycraft_debug_exports");
        Files.createDirectories(directory);
        String fileName = "suguard_" + room + "_" + origin.getX() + "_" + origin.getY() + "_" + origin.getZ() + ".snbt";
        Path path = directory.resolve(fileName);
        Files.writeString(path, root.toString());
        return path;
    }

    private static String stateString(BlockState state) {
        StringBuilder builder = new StringBuilder(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
        if (!state.getProperties().isEmpty()) {
            builder.append('[');
            boolean first = true;
            for (Property<?> property : state.getProperties()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(property.getName()).append('=').append(valueName(state, property));
            }
            builder.append(']');
        }
        return builder.toString();
    }

    private static <T extends Comparable<T>> String valueName(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private record RoomBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }
}
