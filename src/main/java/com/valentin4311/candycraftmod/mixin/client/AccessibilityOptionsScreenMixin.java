package com.valentin4311.candycraftmod.mixin.client;

import com.valentin4311.candycraftmod.client.PropolisVisualSettings;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AccessibilityOptionsScreen.class)
public abstract class AccessibilityOptionsScreenMixin {
    @Inject(method = "options", at = @At("RETURN"), cancellable = true)
    private static void candycraft$addPropolisGlintSize(Options options,
            CallbackInfoReturnable<OptionInstance<?>[]> callback) {
        OptionInstance<?>[] original = callback.getReturnValue();
        OptionInstance<?>[] extended = new OptionInstance<?>[original.length + 1];
        int insertionIndex = original.length;

        for (int index = 0; index < original.length; index++) {
            if (original[index] == options.glintStrength()) {
                insertionIndex = index + 1;
                break;
            }
        }

        System.arraycopy(original, 0, extended, 0, insertionIndex);
        extended[insertionIndex] = PropolisVisualSettings.option();
        System.arraycopy(original, insertionIndex, extended, insertionIndex + 1,
            original.length - insertionIndex);
        callback.setReturnValue(extended);
    }
}
