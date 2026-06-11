package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

public class BasicCandyZombieRenderer extends ZombieRenderer {
    public BasicCandyZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie entity) {
        String texture = "sugarde.png";
        if (entity.getType() == CCEntityTypes.MAGE_SUGUARD.get()) texture = "suguardemage.png";
        else if (entity.getType() == CCEntityTypes.SUGUARD.get() && entity.getMainHandItem().is(CCItems.DYNAMITE.get())) texture = "suguardesoldier.png";
        else if (entity.getType() == CCEntityTypes.CANDY_WOLF.get()) texture = "wolfcandy.png";
        else if (entity.getType() == CCEntityTypes.GUMMY_BUNNY.get()) texture = "bunny.png";
        else if (entity.getType() == CCEntityTypes.GINGERBREAD_MAN.get()) texture = "gingerbread0.png";
        else if (entity.getType() == CCEntityTypes.CANDY_FISH.get()) texture = "fish.png";
        else if (entity.getType() == CCEntityTypes.PINGOUIN.get()) texture = "pingouin0.png";
        else if (entity.getType() == CCEntityTypes.NESSIE.get()) texture = "nessie0.png";
        else if (entity.getType() == CCEntityTypes.DRAGON.get()) texture = "dragons.png";
        else if (entity.getType() == CCEntityTypes.KING_BEETLE.get()) texture = "tamedbeetle.png";
        else if (entity.getType() == CCEntityTypes.MERMAID.get()) texture = "mermaid.png";
        else if (entity.getType() == CCEntityTypes.NOUGAT_GOLEM.get()) texture = "nougatgolem2.png";
        else if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()) texture = "sugardeboss.png";
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + texture);
    }
}
