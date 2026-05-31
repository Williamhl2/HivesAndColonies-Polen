package com.hivesandcolonies.polen.entity.ai.routine;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public final class PolenRoutinePlanner {

    private static final int DEFAULT_SAFE_SPOT_RADIUS = 10;
    private static final int MIN_INTEREST_BRIGHTNESS = 8;

    private PolenRoutinePlanner() {
    }

    public static BlockPos getRoutineTarget(PolenEntity polen, PolenIntent intent) {
        return switch (intent) {
            case SEEK_REST -> findRestTarget(polen);
            case QUIET_CREATION -> findQuietCreationTarget(polen);
            case WANDER_SAFE -> PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
            default -> null;
        };
    }

    public static boolean isRememberedSpotStillValid(PolenEntity polen, BlockPos pos) {
        if (pos == null) {
            return false;
        }

        return (!polen.level().getBlockState(pos).isAir() || pos.closerToCenterThan(polen.position(), 2.0D))
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos);
    }

    public static boolean isSafeInterestSpot(PolenEntity polen, BlockPos pos) {
        if (pos == null || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos)) {
            return false;
        }

        int surfaceY = polen.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
        boolean nearSurface = pos.getY() >= surfaceY - 2;
        boolean brightEnough = polen.level().getMaxLocalRawBrightness(pos.above()) >= MIN_INTEREST_BRIGHTNESS;

        return nearSurface && brightEnough;
    }

    private static BlockPos findRestTarget(PolenEntity polen) {
        if (isRememberedSpotStillValid(polen, polen.getAiState().getRestingPos())
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, polen.getAiState().getRestingPos())) {
            return polen.getAiState().getRestingPos();
        }

        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
    }

    private static BlockPos findQuietCreationTarget(PolenEntity polen) {
        if (isRememberedSpotStillValid(polen, polen.getAiState().getFavoriteSourcePos())
                && isSafeInterestSpot(polen, polen.getAiState().getFavoriteSourcePos())) {
            return polen.getAiState().getFavoriteSourcePos();
        }

        if (isRememberedSpotStillValid(polen, polen.getAiState().getRestingPos())
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, polen.getAiState().getRestingPos())) {
            return polen.getAiState().getRestingPos();
        }

        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
    }
}
