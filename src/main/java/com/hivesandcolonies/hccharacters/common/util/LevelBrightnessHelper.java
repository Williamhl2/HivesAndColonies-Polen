package com.hivesandcolonies.hccharacters.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public final class LevelBrightnessHelper {

    private LevelBrightnessHelper() {
    }

    public static int maxLocalRawBrightness(LevelReader level, BlockPos pos) {
        return level.getMaxLocalRawBrightness(pos, level.getSkyDarken());
    }
}
