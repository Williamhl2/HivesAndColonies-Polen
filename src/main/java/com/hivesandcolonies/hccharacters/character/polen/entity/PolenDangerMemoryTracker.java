package com.hivesandcolonies.hccharacters.character.polen.entity;

import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class PolenDangerMemoryTracker {

    private static final long DANGEROUS_SPOT_MEMORY_DURATION = 24000L;

    private PolenDangerMemoryTracker() {
    }

    public static void rememberDangerousSpot(PolenEntity polen, BlockPos pos) {
        if (pos == null || !(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        polen.getAiState().setDangerousSpotState(
                pos.immutable(),
                serverLevel.getGameTime() + DANGEROUS_SPOT_MEMORY_DURATION
        );
    }

    public static BlockPos getActiveDangerousSpotPos(PolenEntity polen) {
        BlockPos dangerousSpotPos = polen.getAiState().getDangerousSpotPos();
        if (dangerousSpotPos == null) {
            return null;
        }

        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return dangerousSpotPos;
        }

        if (serverLevel.getGameTime() >= polen.getAiState().getDangerousSpotUntilGameTime()) {
            polen.getAiState().setDangerousSpotState(null, 0L);
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
