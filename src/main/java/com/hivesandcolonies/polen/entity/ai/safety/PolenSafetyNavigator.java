package com.hivesandcolonies.polen.entity.ai.safety;

import com.hivesandcolonies.polen.entity.PolenEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public final class PolenSafetyNavigator {

    private static final int LOCAL_ESCAPE_VERTICAL_RANGE = 6;
    private static final int[] LOCAL_Y_OFFSETS = {1, 2, 0, 3, -1, 4, 5, -2, 6};
    private static final int[] SURFACE_Y_OFFSETS = {0, -1, 1, 2, -2};

    private PolenSafetyNavigator() {
    }

    public static boolean isInUnsafeArea(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean unsafe = !PolenSafetyEvaluator.isSafeStandingSpot(polen, currentPos);

        if (PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos)) {
            polen.rememberDangerousSpot(currentPos);
        }

        return unsafe;
    }

    public static boolean shouldSeekSafety(PolenEntity polen) {
        BlockPos currentPos = polen.blockPosition();
        boolean dangerous = PolenSafetyEvaluator.isTrulyDangerousStandingSpot(polen, currentPos);
        if (dangerous) {
            polen.rememberDangerousSpot(currentPos);
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

    public static Vec3 getNearestSafeSpotCenter(PolenEntity polen, int radius) {
        BlockPos pos = findNearbySafeSurfaceSpot(polen, radius);
        return pos == null ? null : Vec3.atCenterOf(pos);
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

    private static double scoreCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate) || polen.isDangerousMemorySpot(candidate)) {
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
}
