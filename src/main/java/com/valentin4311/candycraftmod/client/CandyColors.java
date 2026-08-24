package com.valentin4311.candycraftmod.client;

import net.minecraft.world.phys.Vec3;

/** Shared RGB helpers and candy-dimension fallback palette values. */
public final class CandyColors {
    public static final int CANDY_WORLD_FOG_FALLBACK = 0xEEAABB;
    public static final int CANDY_WORLD_SKY_FALLBACK = 0xFDD8D7;

    private CandyColors() {
    }

    public static Vec3 rgbVec(int color) {
        return new Vec3(((color >> 16) & 255) / 255.0D, ((color >> 8) & 255) / 255.0D, (color & 255) / 255.0D);
    }

    public static int lerpColor(int from, int to, double amount) {
        int fromRed = (from >> 16) & 255;
        int fromGreen = (from >> 8) & 255;
        int fromBlue = from & 255;
        int toRed = (to >> 16) & 255;
        int toGreen = (to >> 8) & 255;
        int toBlue = to & 255;
        int red = (int)Math.round(fromRed + (toRed - fromRed) * amount);
        int green = (int)Math.round(fromGreen + (toGreen - fromGreen) * amount);
        int blue = (int)Math.round(fromBlue + (toBlue - fromBlue) * amount);
        return (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }
}
