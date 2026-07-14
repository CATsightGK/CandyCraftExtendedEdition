package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CandyWeatherEvents {
    private static final ResourceKey<net.minecraft.world.level.Level> CANDY_WORLD = ResourceKey.create(
        Registries.DIMENSION, new ResourceLocation(CandyCraft.MODID, "candy_world"));
    private static final int CAULDRON_SCAN_RADIUS = 6;
    private static final int CAULDRON_SCAN_DIAMETER = CAULDRON_SCAN_RADIUS * 2 + 1;

    private CandyWeatherEvents() {
    }

    @SubscribeEvent
    public static void onWeatherCommand(CommandEvent event) {
        if (!event.getParseResults().getExceptions().isEmpty()
            || !event.getParseResults().getContext().getSource().hasPermission(2)) {
            return;
        }

        String[] command = event.getParseResults().getReader().getString().trim().split("\\s+");
        if (command.length < 2 || command.length > 3 || !"weather".equals(command[0])) {
            return;
        }

        ServerLevel candyWorld = event.getParseResults().getContext().getSource().getServer().getLevel(CANDY_WORLD);
        if (candyWorld == null) {
            return;
        }

        int duration = command.length == 3 ? parseWeatherDuration(command[2]) : defaultWeatherDuration(candyWorld, command[1]);
        if (duration < 0) {
            return;
        }
        switch (command[1]) {
            case "clear" -> candyWorld.setWeatherParameters(duration, 0, false, false);
            case "rain" -> candyWorld.setWeatherParameters(0, duration, true, false);
            case "thunder" -> candyWorld.setWeatherParameters(0, duration, true, true);
            default -> {
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level) || !isCandyWorld(level)) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            scanOneChunkForCauldrons(level, player);
        }
    }

    @SubscribeEvent
    public static void onRightClickCauldron(PlayerInteractEvent.RightClickBlock event) {
        if (!isCandyWorld(event.getLevel()) || !event.getItemStack().is(Items.MILK_BUCKET)
            || !event.getLevel().getBlockState(event.getPos()).is(Blocks.CAULDRON)) {
            return;
        }
        event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
        event.setCanceled(true);
        if (event.getLevel().isClientSide) {
            return;
        }
        ItemStack held = event.getItemStack();
        event.getEntity().setItemInHand(event.getHand(),
            ItemUtils.createFilledResult(held, event.getEntity(), new ItemStack(Items.BUCKET)));
        event.getLevel().setBlockAndUpdate(event.getPos(), CCBlocks.MILK_CAULDRON.get().defaultBlockState()
            .setValue(LayeredCauldronBlock.LEVEL, LayeredCauldronBlock.MAX_FILL_LEVEL));
        event.getEntity().awardStat(net.minecraft.stats.Stats.FILL_CAULDRON);
        event.getLevel().playSound(null, event.getPos(), net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
            net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
        event.getLevel().gameEvent(null, net.minecraft.world.level.gameevent.GameEvent.FLUID_PLACE, event.getPos());
    }

    private static void scanOneChunkForCauldrons(ServerLevel level, ServerPlayer player) {
        int sequence = Math.floorMod((int)(level.getGameTime() + player.getId() * 31L),
            CAULDRON_SCAN_DIAMETER * CAULDRON_SCAN_DIAMETER);
        int chunkX = player.chunkPosition().x + sequence % CAULDRON_SCAN_DIAMETER - CAULDRON_SCAN_RADIUS;
        int chunkZ = player.chunkPosition().z + sequence / CAULDRON_SCAN_DIAMETER - CAULDRON_SCAN_RADIUS;
        if (!level.hasChunk(chunkX, chunkZ)) {
            return;
        }

        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                tickCauldronColumn(level, minX + dx, minZ + dz);
            }
        }
    }

    private static void tickCauldronColumn(ServerLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x,
            level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
        BlockState state = level.getBlockState(pos);
        if (!level.isRaining() || !level.canSeeSky(pos.above())) {
            return;
        }
        Biome.Precipitation precipitation = level.getBiome(pos).value().getPrecipitationAt(pos);
        if (state.is(Blocks.POWDER_SNOW_CAULDRON)) {
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            return;
        }
        if (precipitation != Biome.Precipitation.RAIN) {
            return;
        }
        if (state.is(Blocks.WATER_CAULDRON)) {
            int fill = state.getValue(LayeredCauldronBlock.LEVEL);
            level.setBlockAndUpdate(pos, CCBlocks.MILK_CAULDRON.get().defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, fill));
        } else if (state.is(CCBlocks.MILK_CAULDRON.get())
            && state.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL
            && level.random.nextInt(10) == 0) {
            level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL));
        } else if (state.is(Blocks.CAULDRON) && level.random.nextInt(10) == 0) {
            level.setBlockAndUpdate(pos, CCBlocks.MILK_CAULDRON.get().defaultBlockState());
        }
    }

    private static int parseWeatherDuration(String value) {
        try {
            int seconds = Integer.parseInt(value);
            return seconds >= 1 && seconds <= 1_000_000 ? seconds * 20 : -1;
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private static int defaultWeatherDuration(ServerLevel level, String weather) {
        return switch (weather) {
            case "clear" -> level.random.nextInt(12000) + 6000;
            case "rain" -> level.random.nextInt(12000) + 12000;
            case "thunder" -> level.random.nextInt(12000) + 3600;
            default -> -1;
        };
    }

    private static boolean isCandyWorld(net.minecraft.world.level.LevelAccessor level) {
        if (!(level instanceof net.minecraft.world.level.Level actualLevel)) {
            return false;
        }
        ResourceLocation dimension = actualLevel.dimension().location();
        return CandyCraft.MODID.equals(dimension.getNamespace()) && "candy_world".equals(dimension.getPath());
    }
}
