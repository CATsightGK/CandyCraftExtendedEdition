package com.valentin4311.candycraftmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class JellyFragmentParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseAlpha;

    private JellyFragmentParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites,
            float red, float green, float blue) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.gravity = 0.7F;
        this.friction = 0.84F;
        this.lifetime = 14 + random.nextInt(9);
        this.quadSize = 0.075F + random.nextFloat() * 0.055F;
        this.baseAlpha = 0.68F;
        setColor(red, green, blue);
        setAlpha(baseAlpha);
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);
        float fadeStart = lifetime * 0.55F;
        if (age > fadeStart) {
            setAlpha(baseAlpha * Math.max(0.0F, (lifetime - age) / (lifetime - fadeStart)));
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final float red;
        private final float green;
        private final float blue;

        public Provider(SpriteSet sprites, float red, float green, float blue) {
            this.sprites = sprites;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new JellyFragmentParticle(level, x, y, z, xSpeed, ySpeed, zSpeed,
                sprites, red, green, blue);
        }
    }
}
