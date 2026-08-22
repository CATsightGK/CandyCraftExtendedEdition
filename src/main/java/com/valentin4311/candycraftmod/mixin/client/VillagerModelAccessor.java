package com.valentin4311.candycraftmod.mixin.client;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VillagerModel.class)
public interface VillagerModelAccessor {
    @Accessor("hat")
    ModelPart candycraft$getHat();

    @Accessor("hatRim")
    ModelPart candycraft$getHatRim();
}
