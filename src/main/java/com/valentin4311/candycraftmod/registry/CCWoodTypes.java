package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import java.util.List;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public final class CCWoodTypes {
    public static final WoodType MARSHMALLOW = register("marshmallow");
    public static final WoodType MARSHMALLOW_LIGHT = register("marshmallow_light");
    public static final WoodType MARSHMALLOW_DARK = register("marshmallow_dark");
    public static final WoodType MILK_CHOCOLATE = register("milk_chocolate");
    public static final WoodType WHITE_CHOCOLATE = register("white_chocolate");
    public static final WoodType DARK_CHOCOLATE = register("dark_chocolate");

    private CCWoodTypes() {
    }

    public static List<WoodType> values() {
        return List.of(
            MARSHMALLOW, MARSHMALLOW_LIGHT, MARSHMALLOW_DARK,
            MILK_CHOCOLATE, WHITE_CHOCOLATE, DARK_CHOCOLATE
        );
    }

    private static WoodType register(String name) {
        return WoodType.register(new WoodType(CandyCraft.MODID + ":" + name, BlockSetType.OAK));
    }
}
