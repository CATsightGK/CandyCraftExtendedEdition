package com.valentin4311.candycraftmod.client;

import com.google.common.collect.Maps;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.GummyMouseModel;
import com.valentin4311.candycraftmod.entity.GummyMouseEntity;
import com.valentin4311.candycraftmod.entity.SweetscapeGummyColor;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GummyMouseRenderer extends MobRenderer<GummyMouseEntity, GummyMouseModel<GummyMouseEntity>> {
    private static final Map<SweetscapeGummyColor, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(SweetscapeGummyColor.class), map -> {
        map.put(SweetscapeGummyColor.RED, texture("red_gummy_mouse"));
        map.put(SweetscapeGummyColor.ORANGE, texture("orange_gummy_mouse"));
        map.put(SweetscapeGummyColor.YELLOW, texture("yellow_gummy_mouse"));
        map.put(SweetscapeGummyColor.WHITE, texture("white_gummy_mouse"));
        map.put(SweetscapeGummyColor.GREEN, texture("green_gummy_mouse"));
    });

    public GummyMouseRenderer(EntityRendererProvider.Context context) {
        super(context, new GummyMouseModel<>(context.bakeLayer(GummyMouseModel.LAYER)), 0.25F);
    }

    @Override
    public ResourceLocation getTextureLocation(GummyMouseEntity entity) {
        return TEXTURES.getOrDefault(entity.getColor(), TEXTURES.get(SweetscapeGummyColor.RED));
    }

    @Nullable
    @Override
    protected RenderType getRenderType(GummyMouseEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        ResourceLocation texture = getTextureLocation(entity);
        if (translucent) {
            return RenderType.itemEntityTranslucentCull(texture);
        }
        return bodyVisible ? RenderType.entityTranslucent(texture) : null;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/gummy_mouse/" + name + ".png");
    }
}
