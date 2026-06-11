package com.valentin4311.candycraftmod.world.noise;

import java.util.Random;
import net.minecraft.util.Mth;

public final class LegacyPerlinNoise {
    private final int[] permutations = new int[512];
    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;

    public LegacyPerlinNoise(Random random, boolean useOffset) {
        this.offsetX = useOffset ? random.nextDouble() * 256.0D : 0.0D;
        this.offsetY = useOffset ? random.nextDouble() * 256.0D : 0.0D;
        this.offsetZ = useOffset ? random.nextDouble() * 256.0D : 0.0D;

        for (int i = 0; i < 256; ++i) {
            permutations[i] = i;
        }

        for (int i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i) + i;
            int value = permutations[i];
            permutations[i] = permutations[j];
            permutations[j] = value;
            permutations[i + 256] = permutations[i];
        }
    }

    public double sampleXZ(double x, double z, double frequency) {
        frequency = 1.0D / frequency;
        x += offsetX;
        z += offsetZ;

        int floorX = Mth.floor(x);
        int floorZ = Mth.floor(z);
        int gridX = floorX & 255;
        int gridZ = floorZ & 255;

        x -= floorX;
        z -= floorZ;

        double u = fade(x);
        double w = fade(z);
        int a = permutations[gridX];
        int aa = permutations[a] + gridZ;
        int b = permutations[gridX + 1];
        int ba = permutations[b] + gridZ;

        double front = lerp(u, grad(permutations[aa], x, 0.0D, z), grad(permutations[ba], x - 1.0D, 0.0D, z));
        double back = lerp(u, grad(permutations[aa + 1], x, 0.0D, z - 1.0D), grad(permutations[ba + 1], x - 1.0D, 0.0D, z - 1.0D));
        return lerp(w, front, back) * frequency;
    }

    public double sampleXYZ(double x, double y, double z, double yScale, double yMax) {
        x += offsetX;
        y += offsetY;
        z += offsetZ;

        int floorX = Mth.floor(x);
        int floorY = Mth.floor(y);
        int floorZ = Mth.floor(z);

        x -= floorX;
        y -= floorY;
        z -= floorZ;

        double yOffset;
        if (yScale != 0.0D) {
            double clippedY = yMax >= 0.0D && yMax < y ? yMax : y;
            yOffset = Mth.floor(clippedY / yScale + 1.0000000116860974E-7D) * yScale;
        } else {
            yOffset = 0.0D;
        }

        return sampleXYZ(floorX, floorY, floorZ, x, y - yOffset, z, y);
    }

    private double sampleXYZ(int floorX, int floorY, int floorZ, double localX, double localOffsetY, double localZ, double localY) {
        int gridX = floorX & 255;
        int gridY = floorY & 255;
        int gridZ = floorZ & 255;

        int a = permutations[gridX] + gridY;
        int aa = permutations[a] + gridZ;
        int ab = permutations[a + 1] + gridZ;
        int b = permutations[gridX + 1] + gridY;
        int ba = permutations[b] + gridZ;
        int bb = permutations[b + 1] + gridZ;

        double u = fade(localX);
        double v = fade(localY);
        double w = fade(localZ);

        return Mth.lerp3(
            u,
            v,
            w,
            grad(permutations[aa], localX, localOffsetY, localZ),
            grad(permutations[ba], localX - 1.0D, localOffsetY, localZ),
            grad(permutations[ab], localX, localOffsetY - 1.0D, localZ),
            grad(permutations[bb], localX - 1.0D, localOffsetY - 1.0D, localZ),
            grad(permutations[aa + 1], localX, localOffsetY, localZ - 1.0D),
            grad(permutations[ba + 1], localX - 1.0D, localOffsetY, localZ - 1.0D),
            grad(permutations[ab + 1], localX, localOffsetY - 1.0D, localZ - 1.0D),
            grad(permutations[bb + 1], localX - 1.0D, localOffsetY - 1.0D, localZ - 1.0D)
        );
    }

    private static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double grad(int hash, double x, double y, double z) {
        return switch (hash & 15) {
            case 0 -> x + y;
            case 1 -> -x + y;
            case 2 -> x - y;
            case 3 -> -x - y;
            case 4 -> x + z;
            case 5 -> -x + z;
            case 6 -> x - z;
            case 7 -> -x - z;
            case 8 -> y + z;
            case 9 -> -y + z;
            case 10 -> y - z;
            case 11 -> -y - z;
            case 12 -> y + x;
            case 13 -> -y + z;
            case 14 -> y - x;
            default -> -y - z;
        };
    }
}
