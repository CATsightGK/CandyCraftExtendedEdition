package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WaffleSheepEntity extends Sheep {
    public WaffleSheepEntity(EntityType<? extends WaffleSheepEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.CANDIED_CHERRY.get());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && source.getEntity() != null && random.nextInt(4) == 0) {
            spawnAtLocation(CCItems.WAFFLE_NUGGET.get());
        }
        return super.hurt(source, amount);
    }

    @Nullable
    @Override
    public Sheep getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return CCEntityTypes.WAFFLE_SHEEP.get().create(level);
    }
}
