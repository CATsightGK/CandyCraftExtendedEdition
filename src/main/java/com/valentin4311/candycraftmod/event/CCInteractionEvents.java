package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.LegacySaplingBlock;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMobEffects;
import com.valentin4311.candycraftmod.effect.CloyingSweetFoodHelper;
import com.valentin4311.candycraftmod.world.CCDimensions;
import com.valentin4311.candycraftmod.world.feature.CottonCandyTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Player interaction rules: cloying cake penalty, dungeon-protected blocks,
 * vanilla bed rejection in the candy world, hoe tilling and nougat powder
 * sapling growth, plus the candy-world sleep time skip.
 */
@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCInteractionEvents {
    private static final TagKey<net.minecraft.world.level.block.Block> CANDY_WORLD_BEDS = TagKey.create(
        Registries.BLOCK, new ResourceLocation(CandyCraft.MODID, "candy_world_beds"));

    private CCInteractionEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        applyCloyingCakePenalty(event);
        if (CCDungeonProtectionEvents.isProtectedDungeonInteraction(event.getLevel(), event.getEntity())) {
            return;
        }
        if (tryRejectVanillaBed(event)) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
            return;
        }
        if (tryTillCandySoil(event) || tryGrowCandySapling(event)) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSpawnEggInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().getItem() instanceof ForgeSpawnEggItem) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        if (!CCDimensions.isCandyWorld(level)) {
            return;
        }
        ServerLevel overworld = level.getServer().overworld();
        long dayTime = overworld.getDayTime();
        long nextMorning = dayTime - Math.floorMod(dayTime, 24000L) + 24000L;
        // Custom dimensions use derived level data, whose setDayTime is a no-op.
        overworld.setDayTime(nextMorning);
        event.setTimeAddition(nextMorning);
    }

    private static void applyCloyingCakePenalty(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (level.isClientSide
                || !player.hasEffect(CCMobEffects.CLOYING.get())
                || !player.canEat(false)
                || !(state.getBlock() instanceof CakeBlock)) {
            return;
        }
        if (state.getValue(CakeBlock.BITES) == 0 && event.getItemStack().is(ItemTags.CANDLES)) {
            return;
        }
        player.hurt(player.damageSources().magic(), 0.5F);
    }

    private static boolean tryRejectVanillaBed(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (!CCDimensions.isCandyWorld(level) || !(state.getBlock() instanceof BedBlock)) {
            return false;
        }
        if (state.is(CANDY_WORLD_BEDS)) {
            return false;
        }
        if (!level.isClientSide) {
            event.getEntity().displayClientMessage(
                Component.translatable("message.candycraftmod.vanilla_bed_unresponsive"), true);
        }
        return true;
    }

    private static boolean tryTillCandySoil(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof HoeItem)) {
            return false;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(CCBlocks.PUDDING.get()) && !state.is(CCBlocks.FLOUR.get())) {
            return false;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, CCBlocks.CANDY_FARMLAND.get().defaultBlockState(), 11);
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.hurtAndBreak(1, event.getEntity(), player -> player.broadcastBreakEvent(event.getHand()));
        }
        return true;
    }

    private static boolean tryGrowCandySapling(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(CCItems.NOUGAT_POWDER.get())) {
            return false;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof LegacySaplingBlock) {
            return false;
        }
        if (!state.is(CCBlocks.COTTON_CANDY_SAPLING.get())) {
            return false;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            level.removeBlock(pos, false);
            if (!CottonCandyTreeFeature.generate(serverLevel, level.random, pos)) {
                level.setBlock(pos, state, 4);
                return false;
            }
            if (!event.getEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return true;
    }
}
