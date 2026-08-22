package com.valentin4311.candycraftmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class PropolisMobEffect extends MobEffect {
    public PropolisMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xE7B83D);
        addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            "D43B7B37-8CF5-4A2E-A119-06D36B6559C6",
            -0.30D,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }
}
