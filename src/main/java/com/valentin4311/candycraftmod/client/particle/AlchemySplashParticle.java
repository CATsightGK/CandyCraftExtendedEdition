package com.valentin4311.candycraftmod.client.particle;

import com.valentin4311.candycraftmod.block.entity.AlchemyLiquidKind;
import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;

public class AlchemySplashParticle extends TextureSheetParticle {
    private final AlchemyLiquidKind liquidKind;
    private final boolean translucent;
    private final SpriteSet sprites;
    private final float baseAlpha;

    protected AlchemySplashParticle(ClientLevel level, double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.gravity = 0.09F;
        this.friction = 0.88F;
        this.lifetime = 7 + level.random.nextInt(5);
        // Match the vanilla underwater suspended particle's texture and size,
        // while retaining gravity so this still behaves as a liquid splash.
        this.quadSize = 0.12F * (level.random.nextFloat() * 0.6F + 0.25F);
        this.liquidKind = kindAt(level, BlockPos.containing(x, y, z));
        this.translucent = isTranslucent(liquidKind);
        this.setSprite(sprites.get(level.random));
        float[] color = color(liquidKind);
        this.setColor(color[0], color[1], color[2]);
        this.baseAlpha = translucent ? 0.72F : 1.0F;
        this.setAlpha(this.baseAlpha);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            this.setSpriteFromAge(this.sprites);
            // Fade out over the last 40% of the lifetime so droplets dissolve
            // instead of popping out of existence.
            float fade = 1.0F - Math.max(0.0F,
                (this.age - this.lifetime * 0.6F) / (this.lifetime * 0.4F));
            this.setAlpha(this.baseAlpha * fade);
        }
        if (this.onGround) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return translucent
            ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT
            : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    private static AlchemyLiquidKind kindAt(ClientLevel level, BlockPos pos) {
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 0, 1))) {
            if (level.getBlockEntity(candidate) instanceof AlchemyTableBlockEntity table) {
                return table.getLiquidKind();
            }
        }
        return AlchemyLiquidKind.WATER;
    }

    private static boolean isTranslucent(AlchemyLiquidKind kind) {
        return switch (kind) {
            case GRENADINE, WATER, CARAMEL -> true;
            default -> false;
        };
    }

    private static float[] color(AlchemyLiquidKind kind) {
        float[] base = switch (kind) {
            // Base tints are sampled from each liquid's still texture so the
            // splash reads as the same liquid, just a touch lighter.
            case GRENADINE -> new float[] {0.95F, 0.16F, 0.15F};
            case WATER -> new float[] {0.25F, 0.55F, 1.0F};
            case MILK -> new float[] {0.95F, 0.95F, 0.88F};
            case CHOCOLATE -> new float[] {0.45F, 0.20F, 0.08F};
            case LIQUID_CANDY -> new float[] {0.90F, 0.40F, 0.77F};
            case LAVA -> new float[] {1.0F, 0.32F, 0.02F};
            case CARAMEL -> new float[] {0.92F, 0.46F, 0.10F};
            case NONE -> new float[] {1.0F, 1.0F, 1.0F};
        };
        // Splash droplets read better a bit lighter than the liquid surface.
        for (int i = 0; i < 3; i++) {
            base[i] += (1.0F - base[i]) * 0.35F;
        }
        return base;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed) {
            return new AlchemySplashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
