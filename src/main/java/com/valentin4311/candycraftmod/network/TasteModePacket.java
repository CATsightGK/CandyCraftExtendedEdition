package com.valentin4311.candycraftmod.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record TasteModePacket(boolean enabled) {
    public static void encode(TasteModePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.enabled);
    }

    public static TasteModePacket decode(FriendlyByteBuf buffer) {
        return new TasteModePacket(buffer.readBoolean());
    }

    public static void handle(TasteModePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() != null) {
                context.getSender().getPersistentData().putBoolean(CCTasteNetwork.TASTE_MODE_TAG, packet.enabled);
            }
        });
        context.setPacketHandled(true);
    }
}
