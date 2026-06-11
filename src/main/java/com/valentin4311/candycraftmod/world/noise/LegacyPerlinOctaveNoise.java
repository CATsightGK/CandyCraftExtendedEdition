package com.valentin4311.candycraftmod.world.noise;

import java.util.Random;
import net.minecraft.util.Mth;

public final class LegacyPerlinOctaveNoise {
    private final LegacyPerlinNoise[] octaves;

    public LegacyPerlinOctaveNoise(Random random, int octaveCount, boolean useOffset) {
        this.octaves = new LegacyPerlinNoise[octaveCount];
        for (int i = 0; i < octaveCount; ++i) {
            this.octaves[i] = new LegacyPerlinNoise(random, useOffset);
        }
    }

    public double sampleXZWrapped(double x, double z, double scaleX, double scaleZ) {
        double total = 0.0D;
        double frequency = 1.0D;

        for (LegacyPerlinNoise octave : octaves) {
            double offsetX = x * frequency * scaleX;
            double offsetZ = z * frequency * scaleZ;
            long offsetXCoord = Mth.lfloor(offsetX);
            long offsetZCoord = Mth.lfloor(offsetZ);
            offsetX -= offsetXCoord;
            offsetZ -= offsetZCoord;
            offsetXCoord %= 16777216L;
            offsetZCoord %= 16777216L;
            offsetX += offsetXCoord;
            offsetZ += offsetZCoord;

            total += octave.sampleXZ(offsetX, offsetZ, frequency);
            frequency /= 2.0D;
        }

        return total;
    }

    public double sampleWrapped(double x, double y, double z, double scaleX, double scaleY, double scaleZ) {
        double total = 0.0D;
        double frequency = 1.0D;

        for (LegacyPerlinNoise octave : octaves) {
            double offsetX = x * frequency * scaleX;
            double offsetZ = z * frequency * scaleZ;
            long offsetXCoord = Mth.lfloor(offsetX);
            long offsetZCoord = Mth.lfloor(offsetZ);
            offsetX -= offsetXCoord;
            offsetZ -= offsetZCoord;
            offsetXCoord %= 16777216L;
            offsetZCoord %= 16777216L;
            offsetX += offsetXCoord;
            offsetZ += offsetZCoord;

            double scaledY = y * scaleY * frequency;
            total += octave.sampleXYZ(offsetX, scaledY, offsetZ, scaleY * frequency, scaledY) / frequency;
            frequency /= 2.0D;
        }

        return total;
    }
}
