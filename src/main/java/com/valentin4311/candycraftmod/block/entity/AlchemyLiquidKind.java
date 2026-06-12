package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum AlchemyLiquidKind {
    NONE("none"),
    GRENADINE("grenadine"),
    WATER("water"),
    MILK("milk"),
    CHOCOLATE("chocolate"),
    LIQUID_CANDY("liquid_candy"),
    LAVA("lava"),
    CARAMEL("caramel");

    private final String id;

    AlchemyLiquidKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public ItemStack bucket() {
        return switch (this) {
            case GRENADINE -> new ItemStack(CCItems.GRENADINE_BUCKET.get());
            case WATER -> new ItemStack(Items.WATER_BUCKET);
            case MILK -> new ItemStack(Items.MILK_BUCKET);
            case CHOCOLATE -> new ItemStack(CCSweetscapeItems.LIQUID_CHOCOLATE_BUCKET.get());
            case LIQUID_CANDY -> new ItemStack(CCSweetscapeItems.LIQUID_CANDY_BUCKET.get());
            case LAVA -> new ItemStack(Items.LAVA_BUCKET);
            case CARAMEL -> new ItemStack(CCItems.CARAMEL_BUCKET.get());
            case NONE -> ItemStack.EMPTY;
        };
    }

    public static AlchemyLiquidKind byId(String id) {
        for (AlchemyLiquidKind kind : values()) {
            if (kind.id.equals(id)) {
                return kind;
            }
        }
        return NONE;
    }
}
