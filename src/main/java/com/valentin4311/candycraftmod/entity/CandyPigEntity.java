package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CandyPigEntity extends Pig {
    public CandyPigEntity(EntityType<? extends CandyPigEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new AvoidEntityGoal<Player>(this, Player.class,
            entity -> entity instanceof Player player && shouldAvoidPlayer(player), 10.0F, 1.65D, 2.1D, entity -> true));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.DRAGIBUS.get()) || stack.is(CCItems.DRAGIBUS_STICK.get());
    }

    private boolean shouldAvoidPlayer(Player player) {
        return !player.isCreative() && !player.isSpectator()
            && !player.getInventory().contains(CCItems.GINGERBREAD_EMBLEM.get().getDefaultInstance());
    }

    @Nullable
    @Override
    public Pig getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return CCEntityTypes.CANDY_PIG.get().create(level);
    }
}
