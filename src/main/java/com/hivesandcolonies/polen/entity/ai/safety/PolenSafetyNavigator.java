package com.hivesandcolonies.polen.entity.ai.safety;

import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.magic.PolenMagicController;
import com.hivesandcolonies.polen.entity.ai.routine.PolenRoutinePlanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

public final class PolenSafetyNavigator {

    private static final int LOCAL_ESCAPE_VERTICAL_RANGE = 6;
    private static final int[] LOCAL_Y_OFFSETS = {1, 2, 0, 3, -1, 4, 5, -2, 6};
    private static final int[] SURFACE_Y_OFFSETS = {0, -1, 1, 2, -2};
    private static final int[] EMERGENCY_RELOCATION_RADII = {32, 64, 96};

    private PolenSafetyNavigator() {
    }

    public static boolean isInUnsafeArea(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean unsafe = !PolenSafetyEvaluator.isSafeStandingSpot(polen, currentPos)
                || hasNearbyHostile(polen, 5.0D);

        if (PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)
                || hasNearbyHostile(polen, 5.0D)) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, currentPos);
        }

        return unsafe;
    }

    public static boolean shouldSeekSafety(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean dangerous = PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)
                || hasNearbyHostile(polen, 6.0D);
        if (dangerous) {
            PolenDangerMemoryTracker.rememberDangerousSpot(polen, currentPos);
            return true;
        }

        return PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, currentPos)
                || shouldSeekRainShelter(polen)
                || shouldSeekNightLight(polen);
    }

    public static boolean shouldUseUnsafeDialogue(PolenEntity polen) {
        return PolenSafetyEvaluator.isClaustrophobicStandingSpot(polen, polen.blockPosition())
                || shouldSeekRainShelter(polen)
                || shouldSeekNightLight(polen);
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

    public static boolean shouldSeekRainShelter(PolenEntity polen) {
        return PolenSafetyEvaluator.isExposedToRain(polen.level(), polen.blockPosition());
    }

    public static boolean shouldSeekNightLight(PolenEntity polen) {
        return polen.level().isNight()
                && polen.getAiState().getActiveLightPos() == null
                && PolenRoutinePlanner.isDarkEnoughForLightMagic(polen)
                && !PolenRoutinePlanner.isReadyToIlluminateHere(polen);
    }

    public static BlockPos findNearbyShelteredSpot(PolenEntity polen, int radius) {
        BlockPos origin = polen.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int searchRadius : new int[] {Math.max(6, radius / 2), Math.max(8, radius)}) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    for (int yOffset : LOCAL_Y_OFFSETS) {
                        if (Math.abs(yOffset) > LOCAL_ESCAPE_VERTICAL_RANGE) {
                            continue;
                        }

                        BlockPos candidate = origin.offset(dx, yOffset, dz);
                        double score = scoreShelterCandidate(polen, origin, candidate);
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

        return findSurfaceShelteredSpot(polen, radius);
    }

    public static BlockPos findNearbyNightLightSpot(PolenEntity polen, int radius) {
        BlockPos lightTarget = PolenRoutinePlanner.findLightMagicTarget(polen);
        if (lightTarget != null && lightTarget.distSqr(polen.blockPosition()) <= (double) (radius * radius)) {
            return lightTarget;
        }

        return lightTarget != null ? lightTarget : findNearbySafeSurfaceSpot(polen, radius);
    }

    public static boolean tryBlinkTowardSafeSpot(PolenEntity polen, BlockPos target, int maxDistance) {
        if (target == null) {
            return false;
        }

        polen.getNavigation().stop();
        polen.stopQuietActivity();
        return PolenMagicController.blinkToward(polen, target, maxDistance, true)
                || PolenMagicController.blinkToward(polen, target, maxDistance, false);
    }

    public static boolean tryBlinkTowardStandableSpot(PolenEntity polen, BlockPos target, int maxDistance) {
        if (target == null) {
            return false;
        }

        polen.getNavigation().stop();
        polen.stopQuietActivity();
        return PolenMagicController.blinkToward(polen, target, maxDistance, false);
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

        for (int searchRadius : new int[] {Math.max(6, radius / 2), radius}) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    for (int yOffset : LOCAL_Y_OFFSETS) {
                        if (Math.abs(yOffset) > LOCAL_ESCAPE_VERTICAL_RANGE) {
                            continue;
                        }

                        BlockPos candidate = origin.offset(dx, yOffset, dz);
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

        for (int searchRadius : new int[] {radius, radius * 2, radius * 3}) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

                    for (int yOffset : SURFACE_Y_OFFSETS) {
                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
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

        for (int searchRadius : new int[] {radius / 2, radius, radius * 2}) {
            int effectiveRadius = Math.max(8, searchRadius);
            for (int dx = -effectiveRadius; dx <= effectiveRadius; dx++) {
                for (int dz = -effectiveRadius; dz <= effectiveRadius; dz++) {
                    for (int yOffset : LOCAL_Y_OFFSETS) {
                        BlockPos candidate = origin.offset(dx, yOffset, dz);
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

        for (int searchRadius : EMERGENCY_RELOCATION_RADII) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

                    for (int yOffset : SURFACE_Y_OFFSETS) {
                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
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

    private static BlockPos findSurfaceShelteredSpot(PolenEntity polen, int radius) {
        Level level = polen.level();
        BlockPos origin = polen.blockPosition();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int searchRadius : new int[] {radius, radius * 2}) {
            for (int dx = -searchRadius; dx <= searchRadius; dx++) {
                for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                    int x = origin.getX() + dx;
                    int z = origin.getZ() + dz;
                    int surfaceY = level.getHeight(
                            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                            x,
                            z
                    );

                    for (int yOffset : SURFACE_Y_OFFSETS) {
                        BlockPos candidate = new BlockPos(x, surfaceY + yOffset, z);
                        double score = scoreShelterCandidate(polen, origin, candidate);
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

    private static double scoreCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
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

    private static double scoreShelterCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                || !PolenSafetyEvaluator.isShelteredStandingSpot(polen.level(), candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        if (candidate.getY() > origin.getY()) {
            score -= (candidate.getY() - origin.getY()) * 5.0D;
        }
        if (PolenSafetyEvaluator.hasOverheadCover(polen.level(), candidate)) {
            score -= 8.0D;
        }

        return score;
    }


    private static boolean hasNearbyHostile(PolenEntity polen, double radius) {
        return !polen.level().getEntitiesOfClass(
                Monster.class,
                polen.getBoundingBox().inflate(radius),
                monster -> monster.isAlive() && !monster.isSpectator()
        ).isEmpty();
    }
}
