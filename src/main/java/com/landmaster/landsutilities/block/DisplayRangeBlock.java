package com.landmaster.landsutilities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

public interface DisplayRangeBlock {
    default int displayRangeColor() {
        return 0xFFFF00FF;
    }

    @Nullable
    AABB displayRangeBox(Level level, BlockPos pos);
}
