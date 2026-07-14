package com.valentin4311.candycraftmod.network;

import com.valentin4311.candycraftmod.compat.CuriosCompat;
import com.valentin4311.candycraftmod.menu.EmblemBasketMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public record OpenEmblemBasketPacket() {
    public static void encode(OpenEmblemBasketPacket packet, FriendlyByteBuf buffer) {
    }

    public static OpenEmblemBasketPacket decode(FriendlyByteBuf buffer) {
        return new OpenEmblemBasketPacket();
    }

    public static void handle(OpenEmblemBasketPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && !CuriosCompat.isLoaded()) {
                NetworkHooks.openScreen(player, new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("container.candycraftmod.emblem_basket");
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                        return new EmblemBasketMenu(id, inventory);
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}
