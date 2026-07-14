package com.valentin4311.candycraftmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class MilkRainDropParticle extends TextureSheetParticle {
    protected MilkRainDropParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed * 0.15D;
        this.yd = -0.65D - level.random.nextDouble() * 0.25D;
        this.zd = zSpeed * 0.15D;
        this.gravity = 0.35F;
        this.friction = 0.98F;
        this.lifetime = 18 + level.random.nextInt(8);
        this.quadSize = 0.055F;
        this.setColor(1.0F, 1.0F, 1.0F);
        this.setAlpha(1.0F);
        this.setSprite(sprites.get(level.random));
        this.hasPhysics = true;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new MilkRainDropParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
