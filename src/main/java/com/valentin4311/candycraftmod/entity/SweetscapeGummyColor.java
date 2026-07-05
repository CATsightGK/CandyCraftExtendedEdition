package com.valentin4311.candycraftmod.entity;

import net.minecraft.util.StringRepresentable;
import net.minecraft.util.RandomSource;

public enum SweetscapeGummyColor implements StringRepresentable {
    RED(0, "red", 0xff4530),
    ORANGE(1, "orange", 0xff9b4f),
    YELLOW(2, "yellow", 0xffe563),
    WHITE(3, "white", 0xfffeb0),
    GREEN(4, "green", 0x80e22b);

    private static final SweetscapeGummyColor[] BY_ID = values();

    private final int id;
    private final String name;
    private final int color;

    SweetscapeGummyColor(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public int id() {
        return id;
    }

    public int color() {
        return color;
    }

    public static SweetscapeGummyColor byId(int id) {
        if (id < 0 || id >= BY_ID.length) {
            return RED;
        }
        return BY_ID[id];
    }

    public static SweetscapeGummyColor random(RandomSource random) {
        return BY_ID[random.nextInt(BY_ID.length)];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
