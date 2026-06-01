package com.hivesandcolonies.polen.entity.ai.routine;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.ai.interest.PolenInterestLocator;
import com.hivesandcolonies.polen.entity.ai.interest.PolenInterestTarget;
import com.hivesandcolonies.polen.entity.ai.interest.PolenInterestType;
import com.hivesandcolonies.polen.entity.ai.intent.PolenIntent;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenScoredSpot;
import com.hivesandcolonies.polen.entity.ai.navigation.PolenSpotSelectionHelper;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyNavigator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public final class PolenRoutinePlanner {

    private static final int DEFAULT_SAFE_SPOT_RADIUS = 10;
    private static final int LIGHT_MAGIC_SEARCH_RADIUS = 6;
    private static final int LIGHT_MAGIC_SURFACE_RADIUS = 12;
    private static final int LIGHT_MAGIC_BLINK_DISTANCE = 8;
    private static final int DARK_LIGHT_THRESHOLD = 4;
    private static final int MIN_INTEREST_BRIGHTNESS = 8;
    private static final int[] LIGHT_SPOT_Y_OFFSETS = {0, 1, -1, 2, -2};

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

        return (!polen.level().getBlockState(pos).isAir()
                || PolenSafetyEvaluator.isStandableSpot(polen, pos)
                || pos.closerToCenterThan(polen.position(), 2.0D))
                && !PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos);
    }

    public static boolean isDarkEnoughForLightMagic(PolenEntity polen) {
        return polen.level().getMaxLocalRawBrightness(polen.blockPosition()) <= DARK_LIGHT_THRESHOLD;
    }

    public static boolean isReadyToIlluminateHere(PolenEntity polen) {
        return isDarkEnoughForLightMagic(polen) && isFlatOpenLightSpot(polen, polen.blockPosition());
    }

    public static boolean isNearRestingSpot(PolenEntity polen) {
        return polen.getAiState().getRestingPos() != null
                && polen.getAiState().getRestingPos().closerToCenterThan(polen.position(), 3.0D);
    }

    public static BlockPos findLightMagicTarget(PolenEntity polen) {
        if (!isDarkEnoughForLightMagic(polen)) {
            return null;
        }

        BlockPos origin = polen.blockPosition();

        for (int radius = 1; radius <= LIGHT_MAGIC_SEARCH_RADIUS; radius++) {
            List<PolenScoredSpot> shortlist = PolenSpotSelectionHelper.createShortlist();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }

                    for (int yOffset : LIGHT_SPOT_Y_OFFSETS) {
                        BlockPos candidate = origin.offset(dx, yOffset, dz);
                        if (!isFlatOpenLightSpot(polen, candidate)) {
                            continue;
                        }

                        double score = candidate.distSqr(origin) + polen.level().getMaxLocalRawBrightness(candidate) * 0.5D;
                        PolenSpotSelectionHelper.offerCandidate(shortlist, candidate, score);
                    }
                }
            }

            BlockPos resolved = PolenSpotSelectionHelper.resolveBestReachable(
                    polen,
                    shortlist,
                    LIGHT_MAGIC_BLINK_DISTANCE,
                    false
            );
            if (resolved != null) {
                return resolved;
            }
        }

        return findSurfaceLightMagicTarget(polen);
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
        BlockPos lightMagicTarget = findLightMagicTarget(polen);
        if (lightMagicTarget != null) {
            return lightMagicTarget;
        }

        if (isRememberedSpotStillValid(polen, polen.getAiState().getFavoriteSourcePos())
                && isSafeInterestSpot(polen, polen.getAiState().getFavoriteSourcePos())) {
            return polen.getAiState().getFavoriteSourcePos();
        }

        PolenInterestTarget localSource = PolenInterestLocator.findPreferredInterestOfType(polen, PolenInterestType.SOURCE);
        if (localSource != null && isSafeInterestSpot(polen, localSource.pos())) {
            return localSource.pos();
        }

        if (isRememberedSpotStillValid(polen, polen.getAiState().getRestingPos())
                && PolenSafetyEvaluator.isSafeStandingSpot(polen, polen.getAiState().getRestingPos())) {
            return polen.getAiState().getRestingPos();
        }

        return PolenSafetyNavigator.findNearbySafeSurfaceSpot(polen, DEFAULT_SAFE_SPOT_RADIUS);
    }

    private static boolean isFlatOpenLightSpot(PolenEntity polen, BlockPos center) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, center)
                || !PolenSafetyEvaluator.isNearOutdoorSurface(level, center)
                || level.getMaxLocalRawBrightness(center) > DARK_LIGHT_THRESHOLD
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, center)) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos tile = center.offset(dx, 0, dz);
                if (!PolenSafetyEvaluator.isStandableSpot(polen, tile)
                        || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, tile)
                        || !level.getFluidState(tile.above(2)).isEmpty()
                        || !level.getBlockState(tile.above(2)).getCollisionShape(level, tile.above(2)).isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static BlockPos findSurfaceLightMagicTarget(PolenEntity polen) {
        Level level = polen.level();
        BlockPos origin = polen.blockPosition();

        for (int searchRadius : new int[] {8, LIGHT_MAGIC_SURFACE_RADIUS}) {
            List<PolenScoredSpot> shortlist = PolenSpotSelectionHelper.createShortlist();
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

                    for (int yOffset : LIGHT_SPOT_Y_OFFSETS) {
                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
                        if (!isFlatOpenLightSpot(polen, candidate)) {
                            continue;
                        }

                        double score = candidate.distSqr(origin) + level.getMaxLocalRawBrightness(candidate) * 0.5D;
                        if (candidate.getY() > origin.getY()) {
                            score -= (candidate.getY() - origin.getY()) * 4.0D;
                        }
                        if (level.canSeeSky(candidate)) {
                            score -= 6.0D;
                        }

                        PolenSpotSelectionHelper.offerCandidate(shortlist, candidate, score);
                    }
                }
            }

            BlockPos resolved = PolenSpotSelectionHelper.resolveBestReachable(
                    polen,
                    shortlist,
                    LIGHT_MAGIC_BLINK_DISTANCE,
                    false
            );
            if (resolved != null) {
                return resolved;
            }
        }

        return null;
    }
}
