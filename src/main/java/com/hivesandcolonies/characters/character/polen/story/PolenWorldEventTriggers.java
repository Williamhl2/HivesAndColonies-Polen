package com.hivesandcolonies.characters.character.polen.story;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

public final class PolenWorldEventTriggers {
    private PolenWorldEventTriggers() {}

    public static void onFirstColonyFounded(ServerLevel level, BlockPos pos) {
        PolenMemoryManager.unlockMemory(
                level,
                PolenMemoryType.FIRST_COLONY,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );
    }

    public static void onFirstResidenceClaimed(ServerLevel level, BlockPos pos) {
        PolenMemoryManager.unlockMemory(
                level,
                PolenMemoryType.FIRST_RESIDENCE,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );
    }

    public static void onFirstSourceDiscovered(ServerLevel level, BlockPos pos) {
        PolenMemoryManager.unlockMemory(
                level,
                PolenMemoryType.FIRST_SOURCE,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );
    }
}
