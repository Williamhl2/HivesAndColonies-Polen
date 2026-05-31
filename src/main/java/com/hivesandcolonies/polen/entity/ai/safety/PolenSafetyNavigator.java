package com.hivesandcolonies.polen.entity.ai.safety;

import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.magic.PolenMagicController;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class PolenSafetyNavigator {

    private static final int LOCAL_ESCAPE_VERTICAL_RANGE = 6;
    private static final int[] LOCAL_Y_OFFSETS = {1, 2, 0, 3, -1, 4, 5, -2, 6};
    private static final int[] SURFACE_Y_OFFSETS = {0, -1, 1, 2, -2};
    private static final int[] EMERGENCY_RELOCATION_RADII = {32, 64, 96};
    private static final int MAX_PATH_CHECKS_PER_SEARCH = 32;

    private PolenSafetyNavigator() {
    }

    public static boolean isInUnsafeArea(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean unsafe = !PolenSafetyEvaluator.isSafeStandingSpot(polen, currentPos);

        if (PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, currentPos);
        }

        return unsafe;
    }

    public static boolean shouldSeekSafety(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean dangerous = PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos);
        if (dangerous) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, currentPos);
            return true;
        }

        return PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, currentPos);
    }

    public static boolean shouldUseUnsafeDialogue(PolenEntity polen) {
        return PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, polen.blockPosition());
    }

    public static BlockPos findNearbySafeSurfaceSpot(PolenEntity polen, int radius) {
        BlockPos localSpot = findBestReachableLocalEscapeSpot(polen, Math.max(6, radius));
        if (localSpot != null) {
            return localSpot;
        }

        return findBestReachableSurfaceSpot(polen, radius);
    }

    public static BlockPos findFallbackExplorationSpot(PolenEntity polen, int radius) {
        return findBestReachableExplorationSpot(polen, Math.max(8, radius));
    }

    public static Vec3 getNearestSafeSpotCenter(PolenEntity polen, int radius) {
        BlockPos pos = findNearbySafeSurfaceSpot(polen, radius);
        return pos == null ? null : Vec3.atCenterOf(pos);
    }

    public static boolean tryEmergencyRelocateToSafeSurface(PolenEntity polen) {
        BlockPos target = findEmergencyRelocationSpot(polen);
        if (target == null) {
            return false;
        }

        polen.getNavigation().stop();
        polen.stopQuietActivity();
        return PolenMagicController.blinkToSafety(polen, target);
    }

    private static BlockPos findBestReachableLocalEscapeSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        int pathChecks = 0;
        for (int searchRadius : new int[] {Math.max(6, radius / 2), radius}) {
            for (int dx = -searchRadius; dx <= searchRadius && pathChecks < MAX_PATH_CHECKS_PER_SEARCH; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius && pathChecks < MAX_PATH_CHECKS_PER_SEARCH; dz++) {
                    for (int yOffset : LOCAL_Y_OFFSETS) {
                        if (Math.abs(yOffset) > LOCAL_ESCAPE_VERTICAL_RANGE) {
                            continue;
                        }

                        BlockPos candidate = origin.offset(dx, yOffset, dz);
                        if (!shouldCheckCandidate(origin, candidate, searchRadius)) {
                            continue;
                        }

                        pathChecks++;
                        double candidateScore = scoreCandidate(polen, origin, candidate);
                        if (candidateScore < bestScore) {
                            bestScore = candidateScore;
                            bestPos = candidate.immutable();
                        }
                    }
                }
            }

            if (bestPos != null) {
                return bestPos;
            }
        }

        return null;
    }

    private static BlockPos findBestReachableSurfaceSpot(PolenEntity polen, int radius) {
        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        int pathChecks = 0;
        for (int searchRadius : new int[] {radius, radius * 2, radius * 3}) {
            for (int dx = -searchRadius; dx <= searchRadius && pathChecks < MAX_PATH_CHECKS_PER_SEARCH; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius && pathChecks < MAX_PATH_CHECKS_PER_SEARCH; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

                    for (int yOffset : SURFACE_Y_OFFSETS) {
                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
                        if (!shouldCheckCandidate(origin, candidate, searchRadius)) {
                            continue;
                        }

                        pathChecks++;
                        double candidateScore = scoreCandidate(polen, origin, candidate);
                        if (candidateScore < bestScore) {
                            bestScore = candidateScore;
                            bestPos = candidate.immutable();
                        }
                    }
                }
            }

            if (bestPos != null) {
                return bestPos;
            }
        }

        return null;
    }

    private static BlockPos findBestReachableExplorationSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        int pathChecks = 0;
        for (int searchRadius : new int[] {radius / 2, radius, radius * 2}) {
            int effectiveRadius = Math.max(8, searchRadius);
            for (int dx = -effectiveRadius; dx <= effectiveRadius && pathChecks < MAX_PATH_CHECKS_PER_SEARCH; dx++) {
                for (int dz = -effectiveRadius; dz <= effectiveRadius && pathChecks < MAX_PATH_CHECKS_PER_SEARCH; dz++) {
                    for (int yOffset : LOCAL_Y_OFFSETS) {
                        BlockPos candidate = origin.offset(dx, yOffset, dz);
                        if (!shouldCheckCandidate(origin, candidate, effectiveRadius)) {
                            continue;
                        }

                        pathChecks++;
                        double candidateScore = scoreExplorationCandidate(polen, origin, candidate);
                        if (candidateScore < bestScore) {
                            bestScore = candidateScore;
                            bestPos = candidate.immutable();
                        }
                    }
                }
            }

            if (bestPos != null) {
                return bestPos;
            }
        }

        return null;
    }

    private static BlockPos findEmergencyRelocationSpot(PolenEntity polen) {
        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        int checked = 0;
        for (int searchRadius : EMERGENCY_RELOCATION_RADII) {
            for (int dx = -searchRadius; dx <= searchRadius && checked < MAX_PATH_CHECKS_PER_SEARCH * 2; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius && checked < MAX_PATH_CHECKS_PER_SEARCH * 2; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

                    for (int yOffset : SURFACE_Y_OFFSETS) {
                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
                        if (!shouldCheckCandidate(origin, candidate, searchRadius)) {
                            continue;
                        }

                        checked++;
                        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)) {
                            continue;
                        }

                        double score = candidate.distSqr(origin);
                        if (level.canSeeSky(candidate)) {
                            score -= 8.0D;
                        }

                        if (score < bestScore) {
                            bestScore = score;
                            bestPos = candidate.immutable();
                        }
                    }
                }
            }

            if (bestPos != null) {
                return bestPos;
            }
        }

        return null;
    }

    private static boolean shouldCheckCandidate(BlockPos origin, BlockPos candidate, int searchRadius) {
        if (origin.distSqr(candidate) <= 16.0D) {
            return true;
        }

        int step = Math.max(2, searchRadius / 6);
        return Math.floorMod(candidate.getX() - origin.getX(), step) == 0
                && Math.floorMod(candidate.getZ() - origin.getZ(), step) == 0;
    }

    private static double scoreCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
            return Double.MAX_VALUE;
        }

        Path path = polen.getNavigation().createPath(candidate, 0);
        if (path == null || !path.canReach()) {
            return Double.MAX_VALUE;
        }

        double distanceSqr = candidate.distSqr(origin);
        double score = distanceSqr;

        int climb = candidate.getY() - origin.getY();
        if (climb > 0) {
            score -= climb * 6.0D;
        }

        if (polen.level().canSeeSky(candidate)) {
            score -= 12.0D;
        } else if (PolenSafetyEvaluator.isNearOutdoorSurface(polen.level(), candidate)) {
            score -= 6.0D;
        }

        return score;
    }

    private static double scoreExplorationCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isStandableSpot(polen, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
            return Double.MAX_VALUE;
        }

        Path path = polen.getNavigation().createPath(candidate, 0);
        if (path == null || !path.canReach()) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        score -= Math.max(0, candidate.getY() - origin.getY()) * 10.0D;
        score -= polen.level().getMaxLocalRawBrightness(candidate) * 0.75D;

        if (polen.level().canSeeSky(candidate)) {
            score -= 20.0D;
        } else if (PolenSafetyEvaluator.isNearOutdoorSurface(polen.level(), candidate)) {
            score -= 8.0D;
        }

        return score;
    }
}
