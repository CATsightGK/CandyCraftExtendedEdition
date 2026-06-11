package com.valentin4311.candycraftmod.network;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class CCTasteNetwork {
    private static final String PROTOCOL = "1";
    public static final String TASTE_MODE_TAG = CandyCraft.MODID + ".taste_mode";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(CandyCraft.MODID, "taste"),
        () -> PROTOCOL,
        PROTOCOL::equals,
        PROTOCOL::equals
    );

    private CCTasteNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(TasteModePacket.class, 0)
            .encoder(TasteModePacket::encode)
            .decoder(TasteModePacket::decode)
            .consumerMainThread(TasteModePacket::handle)
            .add();
    }

    public static void setTasteMode(boolean enabled) {
        CHANNEL.sendToServer(new TasteModePacket(enabled));
    }
}
