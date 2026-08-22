package com.valentin4311.candycraftmod.mixin;

import com.valentin4311.candycraftmod.registry.CCToolProperties;
import com.valentin4311.candycraftmod.registry.CCEnchantments;
import com.valentin4311.candycraftmod.registry.CCItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void candycraft$configuredEnchantability(ItemStack stack, CallbackInfoReturnable<Boolean> callback) {
        CCToolProperties.Profile profile = CCToolProperties.get(stack);
        if (profile != null && profile.enchantability() != null) {
            callback.setReturnValue(profile.enchantability() > 0);
        }
    }

    public int getMaxDamage(ItemStack stack) {
        CCToolProperties.Profile profile = CCToolProperties.get(stack);
        Integer durability = profile == null ? null : profile.durability();
        return durability == null ? ((Item)(Object)this).getMaxDamage() : durability;
    }

    public boolean isDamageable(ItemStack stack) {
        CCToolProperties.Profile profile = CCToolProperties.get(stack);
        Integer durability = profile == null ? null : profile.durability();
        return durability == null ? ((Item)(Object)this).canBeDepleted() : durability > 0;
    }

    public int getEnchantmentValue(ItemStack stack) {
        CCToolProperties.Profile profile = CCToolProperties.get(stack);
        Integer enchantability = profile == null ? null : profile.enchantability();
        return enchantability == null ? ((Item)(Object)this).getEnchantmentValue() : enchantability;
    }

    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == CCEnchantments.HONEY_SOURCE.get()
                && (stack.is(CCItemTags.HONEY_SOURCE_TOOLS) || stack.is(CCItemTags.HONEY_SOURCE_RANGED))) {
            return true;
        }
        Boolean configured = CCToolProperties.configuredEnchantmentRule(stack, enchantment);
        return configured != null ? configured : enchantment.category.canEnchant((Item)(Object)this);
    }
}
