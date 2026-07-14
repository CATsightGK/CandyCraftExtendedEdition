package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.item.ForkItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ForkPlayerRenderEvents {
    private static final ThreadLocal<RotationBackup> ROTATION_BACKUP = new ThreadLocal<>();

    private ForkPlayerRenderEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        var player = event.getEntity();
        ForkItemRenderer.beginPlayerRender(player);
        ItemStack stack = player.getMainHandItem().getItem() instanceof ForkItem
            ? player.getMainHandItem()
            : player.getOffhandItem();
        float bobDegrees = ForkClientAnimations.getArmBobDegrees(stack, event.getPartialTick());
        if (bobDegrees != 0.0F) {
            ROTATION_BACKUP.set(new RotationBackup(player.getXRot(), player.xRotO));
            player.setXRot(player.getXRot() + bobDegrees);
            player.xRotO += bobDegrees;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        RotationBackup backup = ROTATION_BACKUP.get();
        if (backup != null) {
            event.getEntity().setXRot(backup.current);
            event.getEntity().xRotO = backup.previous;
            ROTATION_BACKUP.remove();
        }
        ForkItemRenderer.endPlayerRender();
    }

    private record RotationBackup(float current, float previous) {
    }
}
