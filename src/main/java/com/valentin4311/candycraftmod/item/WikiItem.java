package com.valentin4311.candycraftmod.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class WikiItem extends Item {
    public WikiItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> WikiItem::openClientWikiScreen);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    private static void openClientWikiScreen() {
        try {
            Class.forName("com.valentin4311.candycraftmod.client.CandyWikiClientHooks")
                .getMethod("openWikiScreen")
                .invoke(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
