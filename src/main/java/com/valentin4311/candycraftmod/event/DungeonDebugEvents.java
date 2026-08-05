package com.valentin4311.candycraftmod.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySlimeEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.world.feature.JellyDungeonFeature;
import com.valentin4311.candycraftmod.world.feature.SuguardDungeonFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DungeonDebugEvents {
    private static final Map<MinecraftServer, SuguardGenerationJob> SUGUARD_GENERATION_JOBS = new WeakHashMap<>();

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
                context.getSource().sendSuccess(() -> Component.translatable(
                    "message.candycraftmod.debug.dungeons_generated", base.toShortString(),
                    base.offset(240, 0, 0).toShortString()), true);
                return 1;
            }));
        event.getDispatcher().register(Commands.literal("candycraft_debug_suguard_dungeon")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                BlockPos base = BlockPos.containing(context.getSource().getPosition()).offset(0, 1, 0);
                SuguardDungeonFeature.generateDebugShowcase(level, base);
                context.getSource().sendSuccess(() -> Component.translatable(
                    "message.candycraftmod.debug.suguard_dungeon_generated", base.toShortString()), true);
                return 1;
            }));
        LiteralArgumentBuilder<CommandSourceStack> suguardGenerator = Commands.literal("candycraft_suguard_dungeon")
            .requires(source -> source.hasPermission(2));
        suguardGenerator.then(suguardGenerationTarget("full"));
        for (String room : SuguardDungeonFeature.debugRoomNames()) {
            suguardGenerator.then(suguardGenerationTarget(room));
        }
        event.getDispatcher().register(suguardGenerator);
        event.getDispatcher().register(Commands.literal("candycraft_debug_jelly_water_room")
            .requires(source -> source.hasPermission(2))
            .executes(context -> {
                ServerLevel level = context.getSource().getLevel();
                BlockPos base = BlockPos.containing(context.getSource().getPosition()).offset(0, 1, 0);
                JellyDungeonFeature.generateDebugWaterRoom(level, base);
                context.getSource().sendSuccess(() -> Component.translatable(
                    "message.candycraftmod.debug.jelly_water_room_generated", base.toShortString()), true);
                return 1;
            }));
        event.getDispatcher().register(Commands.literal("candycraft_debug_pez_roll")
            .requires(source -> source.hasPermission(2))
            .executes(context -> debugPezRoll(context.getSource().getLevel(), context.getSource().getPlayerOrException(), 32))
            .then(Commands.argument("radius", IntegerArgumentType.integer(1, 128))
                .executes(context -> debugPezRoll(context.getSource().getLevel(), context.getSource().getPlayerOrException(),
                    IntegerArgumentType.getInteger(context, "radius")))));
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
                            context.getSource().sendFailure(Component.translatable("message.candycraftmod.debug.unknown_room"));
                            return 0;
                        }
                        try {
                            Path exported = exportRoom(level, room, origin, bounds);
                            context.getSource().sendSuccess(() -> Component.translatable(
                                "message.candycraftmod.debug.room_exported", room, exported), true);
                            return 1;
                        } catch (IOException e) {
                            context.getSource().sendFailure(Component.translatable(
                                "message.candycraftmod.debug.room_export_failed", e.getMessage()));
                            return 0;
                        }
                    }))));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        SuguardGenerationJob job = SUGUARD_GENERATION_JOBS.get(server);
        if (job == null) {
            return;
        }
        try {
            if (job.runNext(server.overworld())) {
                SUGUARD_GENERATION_JOBS.remove(server);
                job.source.sendSuccess(() -> Component.translatable(
                    "message.candycraftmod.debug.suguard_generation_finished",
                    job.target, job.origin.toShortString()), true);
            }
        } catch (RuntimeException exception) {
            SUGUARD_GENERATION_JOBS.remove(server);
            job.source.sendFailure(Component.translatable(
                "message.candycraftmod.debug.suguard_generation_failed", exception.getMessage()));
        }
    }

    private static LiteralArgumentBuilder<CommandSourceStack> suguardGenerationTarget(String target) {
        return Commands.literal(target)
            .executes(context -> generateSuguardTarget(
                context.getSource(), target, BlockPos.containing(context.getSource().getPosition()).below()))
            .then(Commands.argument("origin", BlockPosArgument.blockPos())
                .executes(context -> generateSuguardTarget(
                    context.getSource(), target, BlockPosArgument.getLoadedBlockPos(context, "origin"))));
    }

    private static int generateSuguardTarget(CommandSourceStack source, String target, BlockPos origin) {
        MinecraftServer server = source.getServer();
        ServerLevel overworld = server.overworld();
        if (source.getLevel() != overworld) {
            source.sendFailure(Component.translatable("message.candycraftmod.debug.suguard_overworld_only"));
            return 0;
        }
        if (SUGUARD_GENERATION_JOBS.containsKey(server)) {
            source.sendFailure(Component.translatable("message.candycraftmod.debug.suguard_generation_busy"));
            return 0;
        }

        List<SuguardDungeonFeature.DebugGenerationStep> steps = "full".equals(target)
            ? SuguardDungeonFeature.debugDungeonSteps(origin)
            : List.of(new SuguardDungeonFeature.DebugGenerationStep(target, origin));
        if (!"full".equals(target) && !SuguardDungeonFeature.debugRoomNames().contains(target)) {
            source.sendFailure(Component.translatable("message.candycraftmod.debug.unknown_room"));
            return 0;
        }

        SUGUARD_GENERATION_JOBS.put(server, new SuguardGenerationJob(source, target, origin, steps));
        source.sendSuccess(() -> Component.translatable(
            "message.candycraftmod.debug.suguard_generation_started", target, origin.toShortString()), true);
        return 1;
    }

    private static int debugPezRoll(ServerLevel level, ServerPlayer player, int radius) {
        BasicCandySlimeEntity pez = level.getEntitiesOfClass(BasicCandySlimeEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity.getType() == CCEntityTypes.PEZ_JELLY.get())
            .stream()
            .min((a, b) -> Double.compare(a.distanceToSqr(player), b.distanceToSqr(player)))
            .orElse(null);
        if (pez == null) {
            player.sendSystemMessage(Component.translatable("message.candycraftmod.debug.pez_not_found", radius));
            return 0;
        }
        LivingEntity target = pez.getTarget() != null ? pez.getTarget() : player;
        if (!pez.debugStartPezRoll(target)) {
            player.sendSystemMessage(Component.translatable("message.candycraftmod.debug.pez_roll_failed"));
            return 0;
        }
        player.sendSystemMessage(Component.translatable("message.candycraftmod.debug.pez_roll_started", pez.getId()));
        return 1;
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
            case "boss", "boss_key_north", "boss_key_south", "boss_key_west" ->
                new RoomBounds(-21, -2, -21, 21, 36, 21);
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

    private static final class SuguardGenerationJob {
        private final CommandSourceStack source;
        private final String target;
        private final BlockPos origin;
        private final List<SuguardDungeonFeature.DebugGenerationStep> steps;
        private int index;
        private boolean clearing = true;

        private SuguardGenerationJob(CommandSourceStack source, String target, BlockPos origin,
                List<SuguardDungeonFeature.DebugGenerationStep> steps) {
            this.source = source;
            this.target = target;
            this.origin = origin;
            this.steps = List.copyOf(steps);
        }

        private boolean runNext(ServerLevel level) {
            SuguardDungeonFeature.DebugGenerationStep step = steps.get(index);
            boolean success = clearing
                ? SuguardDungeonFeature.clearDebugRoom(level, step.origin(), step.room())
                : SuguardDungeonFeature.placeDebugRoom(level, step.origin(), step.room());
            if (!success) {
                throw new IllegalStateException("Unknown Suguard room: " + step.room());
            }

            index++;
            if (index < steps.size()) {
                return false;
            }
            if (clearing) {
                clearing = false;
                index = 0;
                return false;
            }
            return true;
        }
    }

    private record RoomBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
    }
}
