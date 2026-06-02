package com.hivesandcolonies.polen.entity.ai.navigation.search.shelter;

import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.polen.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class PolenShelterSpotHelper {
    private static final int[] LOCAL_Y_OFFSETS = {0, 1, -1, 2, -2, 3, -3, 4};
    private static final int NEARBY_LIGHT_RADIUS = 4;

    private PolenShelterSpotHelper() {
    }

    public static BlockPos findNearbyShelterSpot(PolenEntity polen, int radius) {
        if (polen == null) {
            return null;
        }

        BlockPos origin = polen.blockPosition();
        BlockPos bestSpot = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy : LOCAL_Y_OFFSETS) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    double score = scoreShelterCandidate(polen, origin, candidate);
                    if (score < bestScore) {
                        bestScore = score;
                        bestSpot = candidate.immutable();
                    }
                }
            }
        }

        return bestSpot;
    }

    private static double scoreShelterCandidate(PolenEntity polen, BlockPos origin, BlockPos candidate) {
        Level level = polen.level();
        if (!PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)
                || candidate.distSqr(origin) < 1.0D) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(origin);
        score -= level.getMaxLocalRawBrightness(candidate) * 1.35D;

        if (PolenSafetyEvaluator.hasOverheadCover(level, candidate)) {
            score -= 4.0D;
        }

        if (!level.canSeeSky(candidate.above())) {
            score -= 1.5D;
        }

        PolenShelterKind shelterKind = PolenShelterContextResolver.resolveShelterKind(level, candidate);
        if (shelterKind == PolenShelterKind.HOUSE) {
            score -= 16.0D;
        } else if (shelterKind == PolenShelterKind.TREE) {
            score -= 8.0D;
        } else if (shelterKind == PolenShelterKind.ROOF) {
            score -= 5.0D;
        }

        if (hasNearbyInterestingLight(level, candidate)) {
            score -= 10.0D;
        }

        return score;
    }

    private static boolean hasNearbyInterestingLight(Level level, BlockPos origin) {
        for (int dx = -NEARBY_LIGHT_RADIUS; dx <= NEARBY_LIGHT_RADIUS; dx++) {
            for (int dz = -NEARBY_LIGHT_RADIUS; dz <= NEARBY_LIGHT_RADIUS; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    if (state.is(ModBlocks.POLEN_LANTERN.get())) {
                        continue;
                    }

                    if (state.getLightEmission() >= 10) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
