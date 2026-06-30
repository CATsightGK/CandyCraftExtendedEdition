package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WikiCommandEvents {
    private WikiCommandEvents() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("candywiki")
            .executes(context -> giveWiki(context.getSource().getPlayerOrException())));
    }

    private static int giveWiki(ServerPlayer player) {
        ItemStack wiki = new ItemStack(CCItems.WIKI.get());
        if (player.getInventory().contains(wiki)) {
            player.displayClientMessage(Component.translatable("chat.candycraftmod.wiki_fail"), true);
            return 0;
        }
        if (!player.getInventory().add(wiki)) {
            player.displayClientMessage(Component.translatable("chat.candycraftmod.wiki_room"), true);
            return 0;
        }
        player.displayClientMessage(Component.translatable("chat.candycraftmod.wiki_ok"), true);
        return 1;
    }
}
