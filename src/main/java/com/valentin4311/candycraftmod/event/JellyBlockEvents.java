package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.JellyBlock;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class JellyBlockEvents {
    private JellyBlockEvents() {
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Level level = player.level();
        if (level.getBlockState(player.blockPosition().below()).is(CCBlocks.PURPLE_TRAMPOJELLY.get())
            || level.getBlockState(player.blockPosition()).is(CCBlocks.PURPLE_TRAMPOJELLY.get())) {
            JellyBlock.releasePurpleJump(player);
        }
    }
}
