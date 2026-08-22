package com.valentin4311.candycraftmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class CloyingMobEffect extends MobEffect {
    public CloyingMobEffect() {
        super(MobEffectCategory.HARMFUL, 0xD8D6D0);
        addAttributeModifier(
            Attributes.ATTACK_DAMAGE,
            "FE8C948D-D968-44C8-BE65-7EEAD3A6B41D",
            -2.0D,
            AttributeModifier.Operation.ADDITION
        );
    }
}
