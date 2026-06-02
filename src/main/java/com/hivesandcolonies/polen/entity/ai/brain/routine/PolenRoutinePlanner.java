package com.hivesandcolonies.polen.entity.ai.brain.routine;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestLocator;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestTarget;
import com.hivesandcolonies.polen.entity.ai.brain.interest.PolenInterestType;
import com.hivesandcolonies.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.navigation.search.light.PolenLightSpotHelper;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public final class PolenRoutinePlanner {

    private static final int DEFAULT_SAFE_SPOT_RADIUS = 10;
    private static final int MIN_INTEREST_BRIGHTNESS = 8;
    private static final int[] ROUTINE_ANCHOR_Y_OFFSETS = {0, 1, -1, 2, -2};
    private static final int ROUTINE_ANCHOR_RADIUS = 2;

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

        return PolenSafetyEvaluator.isSafeStandingSpot(polen, pos)
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos);
    }

    public static BlockPos normalizeRestingAnchor(PolenEntity polen, BlockPos preferredPos) {
        return resolveRoutineAnchor(polen, preferredPos, ROUTINE_ANCHOR_RADIUS, true);
    }

    public static BlockPos normalizeQuietCreationAnchor(PolenEntity polen, BlockPos preferredPos) {
        return resolveRoutineAnchor(polen, preferredPos, ROUTINE_ANCHOR_RADIUS, true);
    }

    public static boolean isDarkEnoughForLightMagic(PolenEntity polen) {
        return PolenLightSpotHelper.isDarkEnoughForLightMagic(polen);
    }

    public static boolean isReadyToIlluminateHere(PolenEntity polen) {
        return PolenLightSpotHelper.isReadyToIlluminateHere(polen);
    }

    public static boolean isNearRestingSpot(PolenEntity polen) {
        return polen.getAiState().getRestingPos() != null
                && polen.getAiState().getRestingPos().closerToCenterThan(polen.position(), 3.0D);
    }

    public static BlockPos findLightMagicTarget(PolenEntity polen) {
        return PolenLightSpotHelper.findLightMagicTarget(polen);
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
        BlockPos normalizedRestingPos = normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null) {
            if (!normalizedRestingPos.equals(polen.getAiState().getRestingPos())) {
                polen.getAiState().setRestingPos(normalizedRestingPos);
            }
            return normalizedRestingPos;
        }

        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
    }

    private static BlockPos findQuietCreationTarget(PolenEntity polen) {
        BlockPos lightMagicTarget = findLightMagicTarget(polen);
        if (lightMagicTarget != null) {
            return lightMagicTarget;
        }

        BlockPos rememberedSourceAnchor = normalizeQuietCreationAnchor(polen, polen.getAiState().getFavoriteSourcePos());
        if (rememberedSourceAnchor != null
                && isSafeInterestSpot(polen, polen.getAiState().getFavoriteSourcePos())) {
            return rememberedSourceAnchor;
        }

        PolenInterestTarget localSource = PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.SOURCE);
        if (localSource != null && isSafeInterestSpot(polen, localSource.pos())) {
            BlockPos localSourceAnchor = normalizeQuietCreationAnchor(polen, localSource.pos());
            if (localSourceAnchor != null) {
                return localSourceAnchor;
            }
        }

        BlockPos normalizedRestingPos = normalizeRestingAnchor(polen, polen.getAiState().getRestingPos());
        if (normalizedRestingPos != null) {
            return normalizedRestingPos;
        }

        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
    }

    private static BlockPos resolveRoutineAnchor(
            PolenEntity polen,
            BlockPos preferredPos,
            int radius,
            boolean requireSafe
    ) {
        if (polen == null || preferredPos == null) {
            return null;
        }

        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy : ROUTINE_ANCHOR_Y_OFFSETS) {
                    BlockPos candidate = preferredPos.offset(dx, dy, dz);
                    boolean valid = requireSafe
                            ? PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            : PolenSafetyEvaluator.isStandableSpot(polen, candidate);
                    if (!valid || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
                        continue;
                    }

                    double score = candidate.distSqr(preferredPos) + candidate.distSqr(polen.blockPosition()) * 0.15D;
                    if (candidate.getY() == preferredPos.getY()) {
                        score -= 0.5D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = candidate.immutable();
                    }
                }
            }
        }

        return bestPos;
    }
}
