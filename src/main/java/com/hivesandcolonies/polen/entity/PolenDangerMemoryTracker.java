package com.hivesandcolonies.polen.entity;

import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.util.PolenNbtHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

public final class PolenDangerMemoryTracker {

    private static final long DANGEROUS_SPOT_MEMORY_DURATION = 24000L;

    private PolenDangerMemoryTracker() {
    }

    public static void save(PolenEntity polen, CompoundTag tag, String posKey, String untilKey) {
        PolenNbtHelper.saveBlockPos(tag, posKey, getActiveDangerousSpotPos(polen));
        tag.putLong(untilKey, polen.getDangerousSpotUntilGameTime());
    }

    public static void load(PolenEntity polen, CompoundTag tag, String posKey, String untilKey) {
        polen.setDangerousSpotState(
                PolenNbtHelper.loadBlockPos(tag, posKey),
                Math.max(0L, tag.getLong(untilKey))
        );
    }

    public static void rememberDangerousSpot(PolenEntity polen, BlockPos pos) {
        if (pos == null || !(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        polen.setDangerousSpotState(
                pos.immutable(),
                serverLevel.getGameTime() + DANGEROUS_SPOT_MEMORY_DURATION
        );
    }

    public static BlockPos getActiveDangerousSpotPos(PolenEntity polen) {
        BlockPos dangerousSpotPos = polen.getDangerousSpotPosRaw();
        if (dangerousSpotPos == null) {
            return null;
        }

        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return dangerousSpotPos;
        }

        if (serverLevel.getGameTime() >= polen.getDangerousSpotUntilGameTime()) {
            polen.setDangerousSpotState(null, 0L);
            return null;
        }

        return dangerousSpotPos;
    }

    public static boolean isDangerousMemorySpot(PolenEntity polen, BlockPos pos, double radius) {
        return PolenSafetyEvaluator.isDangerousMemorySpot(
                getActiveDangerousSpotPos(polen),
                pos,
                radius
        );
    }

    public static boolean isDangerousMemorySpot(PolenEntity polen, BlockPos pos) {
        return isDangerousMemorySpot(polen, pos, polen.getDangerousSpotAvoidRadius());
    }
}
