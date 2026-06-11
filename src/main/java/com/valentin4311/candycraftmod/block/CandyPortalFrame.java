package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

final class CandyPortalFrame {
    private final BlockPos origin;
    private final Direction.Axis axis;
    private final int width;
    private final int height;

    CandyPortalFrame(BlockPos origin, Direction.Axis axis, int width, int height) {
        this.origin = origin;
        this.axis = axis;
        this.width = width;
        this.height = height;
    }

    BlockPos origin() {
        return origin;
    }

    Direction.Axis axis() {
        return axis;
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }
}
