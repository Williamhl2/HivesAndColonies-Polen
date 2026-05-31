package com.hivesandcolonies.polen.entity.ai.interest;

import com.hivesandcolonies.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.safety.PolenSafetyEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PolenInterestLocator {

    private static final int DEFAULT_LOCAL_RADIUS = 8;
    private static final int DEFAULT_LOCAL_HEIGHT = 3;

    private PolenInterestLocator() {
    }

    public static PolenInterestTarget findPreferredInterest(PolenEntity polen, boolean includeSource) {
        PolenInterestTarget remembered = findRememberedInterest(polen, includeSource);
        if (remembered != null) {
            return remembered;
        }

        return findNearestLocalInterest(polen, DEFAULT_LOCAL_RADIUS, DEFAULT_LOCAL_HEIGHT, includeSource);
    }

    public static PolenInterestTarget findRememberedInterest(PolenEntity polen, boolean includeSource) {
        PolenInterestTarget best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        best = pickCloserRememberedInterest(
                polen,
                best,
                bestDistanceSqr,
                polen.getAiState().getFavoriteFlowerPos(),
                PolenInterestType.FLOWER
        );
        if (best != null) {
            bestDistanceSqr = best.pos().distSqr(polen.blockPosition());
        }

        best = pickCloserRememberedInterest(
                polen,
                best,
                bestDistanceSqr,
                polen.getAiState().getFavoriteHivePos(),
                PolenInterestType.HIVE
        );
        if (best != null) {
            bestDistanceSqr = best.pos().distSqr(polen.blockPosition());
        }

        if (includeSource) {
            best = pickCloserRememberedInterest(
                    polen,
                    best,
                    bestDistanceSqr,
                    polen.getAiState().getFavoriteSourcePos(),
                    PolenInterestType.SOURCE
            );
        }

        return best;
    }

    public static PolenInterestTarget findNearestLocalInterest(
            PolenEntity polen,
            int radius,
            int height,
            boolean includeSource
    ) {
        BlockPos origin = polen.blockPosition();
        PolenInterestTarget best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -height, -radius),
                origin.offset(radius, height, radius)
        )) {
            PolenInterestType type = classify(polen.level().getBlockState(pos), includeSource);
            if (type == null || !isInterestTargetSafe(polen, pos)) {
                continue;
            }

            double distanceSqr = pos.distSqr(origin);
            if (distanceSqr < bestDistanceSqr) {
                bestDistanceSqr = distanceSqr;
                best = new PolenInterestTarget(pos.immutable(), type);
            }
        }

        return best;
    }

    public static boolean isSourceLike(BlockState state) {
        return state.is(Blocks.ENCHANTING_TABLE)
                || state.is(Blocks.AMETHYST_BLOCK)
                || state.is(Blocks.BUDDING_AMETHYST)
                || state.is(Blocks.AMETHYST_CLUSTER);
    }

    public static PolenInterestType classify(BlockState state, boolean includeSource) {
        if (state.is(BlockTags.FLOWERS)) {
            return PolenInterestType.FLOWER;
        }

        if (state.is(Blocks.BEE_NEST) || state.is(Blocks.BEEHIVE)) {
            return PolenInterestType.HIVE;
        }

        if (includeSource && isSourceLike(state)) {
            return PolenInterestType.SOURCE;
        }

        return null;
    }

    private static PolenInterestTarget pickCloserRememberedInterest(
            PolenEntity polen,
            PolenInterestTarget currentBest,
            double currentBestDistanceSqr,
            BlockPos candidate,
            PolenInterestType type
    ) {
        if (!isRememberedInterestStillValid(polen, candidate)) {
            return currentBest;
        }

        double distanceSqr = candidate.distSqr(polen.blockPosition());
        if (currentBest == null || distanceSqr < currentBestDistanceSqr) {
            return new PolenInterestTarget(candidate.immutable(), type);
        }

        return currentBest;
    }

    private static boolean isRememberedInterestStillValid(PolenEntity polen, BlockPos pos) {
        if (pos == null || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos)) {
            return false;
        }

        return !polen.level().getBlockState(pos).isAir() || pos.closerToCenterThan(polen.position(), 2.0D);
    }

    private static boolean isInterestTargetSafe(PolenEntity polen, BlockPos pos) {
        if (PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos)) {
            return false;
        }

        return PolenSafetyEvaluator.isSafeStandingSpot(polen, pos)
                || PolenSafetyEvaluator.isSafeStandingSpot(polen, pos.above())
                || PolenSafetyEvaluator.isSafeStandingSpot(polen, pos.below());
    }
}
