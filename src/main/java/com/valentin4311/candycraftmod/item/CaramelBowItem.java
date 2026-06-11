package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.entity.HoneyArrowEntity;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CaramelBowItem extends BowItem {
    private static final Predicate<ItemStack> HONEY_ARROWS = stack -> stack.is(CCItems.HONEY_ARROW.get());

    public CaramelBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return HONEY_ARROWS;
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return HONEY_ARROWS;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int usedTicks = getUseDuration(stack) - timeLeft;
        int fasterTimeLeft = getUseDuration(stack) - usedTicks * 2;
        super.releaseUsing(stack, level, entity, Math.max(0, fasterTimeLeft));
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow) {
        if (arrow instanceof HoneyArrowEntity) {
            return arrow;
        }
        if (arrow.getOwner() instanceof net.minecraft.world.entity.LivingEntity living) {
            return new HoneyArrowEntity(arrow.level(), living);
        }
        return new HoneyArrowEntity(arrow.level(), arrow.getX(), arrow.getY(), arrow.getZ());
    }
}
