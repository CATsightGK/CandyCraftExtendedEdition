package com.valentin4311.candycraftmod.client;

import com.google.common.collect.Maps;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.GummyBearEntity;
import com.valentin4311.candycraftmod.entity.SweetscapeGummyColor;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GummyBearRenderer extends MobRenderer<GummyBearEntity, PolarBearModel<GummyBearEntity>> {
    private static final Map<SweetscapeGummyColor, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(SweetscapeGummyColor.class), map -> {
        map.put(SweetscapeGummyColor.RED, texture("red_gummy_bear"));
        map.put(SweetscapeGummyColor.ORANGE, texture("orange_gummy_bear"));
        map.put(SweetscapeGummyColor.YELLOW, texture("yellow_gummy_bear"));
        map.put(SweetscapeGummyColor.WHITE, texture("white_gummy_bear"));
        map.put(SweetscapeGummyColor.GREEN, texture("green_gummy_bear"));
    });

    public GummyBearRenderer(EntityRendererProvider.Context context) {
        super(context, new PolarBearModel<>(context.bakeLayer(ModelLayers.POLAR_BEAR)), 0.9F);
    }

    @Override
    public ResourceLocation getTextureLocation(GummyBearEntity entity) {
        return TEXTURES.getOrDefault(entity.getColor(), TEXTURES.get(SweetscapeGummyColor.RED));
    }

    @Nullable
    @Override
    protected RenderType getRenderType(GummyBearEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        ResourceLocation texture = getTextureLocation(entity);
        if (translucent) {
            return RenderType.itemEntityTranslucentCull(texture);
        }
        return bodyVisible ? RenderType.entityTranslucent(texture) : null;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/gummy_bear/" + name + ".png");
    }
}
