package com.valentin4311.candycraftmod.world.biome.fractal;

public class LayerOceanTemperature extends Layer {
    public LayerOceanTemperature() {
        super(0);
    }

    @Override
    protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
        return forEach(x, z, width, length, (input, ix, iz) -> {
            double d = smoothNoise((ix + x) / 8.0D, (iz + z) / 8.0D);
            if (d > 0.4D) return DummyBiome.WARM_OCEAN.biomeInfo;
            if (d > 0.2D) return DummyBiome.LUKEWARM_OCEAN.biomeInfo;
            if (d < -0.4D) return DummyBiome.FROZEN_OCEAN.biomeInfo;
            if (d < -0.2D) return DummyBiome.COLD_OCEAN.biomeInfo;
            return DummyBiome.OCEAN.biomeInfo;
        });
    }

    private double smoothNoise(double x, double z) {
        int x0 = (int)Math.floor(x);
        int z0 = (int)Math.floor(z);
        double tx = fade(x - x0);
        double tz = fade(z - z0);
        double a = randomUnit(x0, z0);
        double b = randomUnit(x0 + 1, z0);
        double c = randomUnit(x0, z0 + 1);
        double d = randomUnit(x0 + 1, z0 + 1);
        return lerp(tz, lerp(tx, a, b), lerp(tx, c, d));
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    private double randomUnit(int x, int z) {
        long h = 0x9E3779B97F4A7C15L;
        h ^= x * 0xC2B2AE3D27D4EB4FL;
        h = Long.rotateLeft(h, 27) * 0x94D049BB133111EBL;
        h ^= z * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return ((h >>> 11) * 0x1.0p-53D) * 2.0D - 1.0D;
    }
}
