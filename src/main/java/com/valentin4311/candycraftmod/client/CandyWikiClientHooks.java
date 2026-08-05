package com.valentin4311.candycraftmod.client;

import net.minecraft.client.Minecraft;

public final class CandyWikiClientHooks {
    private CandyWikiClientHooks() {
    }

    public static void openWikiScreen() {
        Minecraft.getInstance().setScreen(new CandyWikiScreen());
    }
}
