package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

public class BasicCandySlimeRenderer extends SlimeRenderer {
    public BasicCandySlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        String texture = "sprinterjelly.png";
        if (entity.getType() == CCEntityTypes.RED_JELLY.get()) texture = "kamikazejelly.png";
        else if (entity.getType() == CCEntityTypes.TORNADO_JELLY.get()) texture = "tornadojelly.png";
        else if (entity.getType() == CCEntityTypes.PEZ_JELLY.get()) texture = "candyboss5.png";
        else if (entity.getType() == CCEntityTypes.KING_SLIME.get()) texture = "candyboss6.png";
        else if (entity.getType() == CCEntityTypes.JELLY_QUEEN.get()) texture = "candyboss.png";
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + texture);
    }
}
