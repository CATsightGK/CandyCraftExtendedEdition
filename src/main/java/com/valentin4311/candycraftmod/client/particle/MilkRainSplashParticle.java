package com.valentin4311.candycraftmod.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class MilkRainSplashParticle extends TextureSheetParticle {
    private static final float[] CHOCOLATE = {0.612F, 0.357F, 0.235F};
    private static final float[] CREAM = {1.0F, 0.878F, 0.639F};

    protected MilkRainSplashParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd *= 0.3D;
        this.yd = Math.random() * 0.2D + 0.1D;
        this.zd *= 0.3D;
        this.setSize(0.01F, 0.01F);
        this.gravity = 0.06F;
        this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
        float[] color = level.random.nextBoolean() ? CHOCOLATE : CREAM;
        this.setColor(color[0], color[1], color[2]);
        this.setSprite(sprites.get(level.random));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
            return;
        }

        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;
        if (this.onGround) {
            if (Math.random() < 0.5D) {
                this.remove();
            }
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }

        BlockPos pos = BlockPos.containing(this.x, this.y, this.z);
        BlockState state = this.level.getBlockState(pos);
        FluidState fluid = this.level.getFluidState(pos);
        double surfaceHeight = Math.max(
            state.getCollisionShape(this.level, pos).max(Direction.Axis.Y,
                this.x - pos.getX(), this.z - pos.getZ()),
            fluid.getHeight(this.level, pos));
        if (surfaceHeight > 0.0D && this.y < pos.getY() + surfaceHeight) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new MilkRainSplashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
