package com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModBlocks;
import com.hivesandcolonies.hccharacters.character.polen.block.PolenBeeBedBlock;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class PolenBedLocator {
    private static final int BED_ACCESS_RADIUS = 3;
    private static final double POLEN_BEE_BED_PRIORITY_BONUS = 10000.0D;

    private PolenBedLocator() {
    }

    public static PolenBedTarget findNearestBedTarget(
            PolenEntity polen,
            BlockPos origin,
            int horizontalRadius,
            int verticalRadius,
            boolean beeBedOnly
    ) {
        if (polen == null || origin == null) {
            return null;
        }

        Level level = polen.level();
        PolenBedTarget bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    boolean isBeeBed = isPolenBeeBedState(state);
                    if (beeBedOnly && !isBeeBed) {
                        continue;
                    }
                    if (!isUsableBedState(state)) {
                        continue;
                    }

                    BlockPos normalizedBedPos = normalizeBedPos(level, candidate);
                    BlockPos anchorPos = normalizeBedAnchorPos(level, candidate);
                    BlockPos accessPos = findBestBedAccessPos(polen, normalizedBedPos);
                    if (normalizedBedPos == null || anchorPos == null || accessPos == null) {
                        continue;
                    }

                    double score = normalizedBedPos.distSqr(origin);
                    if (isBeeBed) {
                        score -= POLEN_BEE_BED_PRIORITY_BONUS;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestTarget = new PolenBedTarget(anchorPos, normalizedBedPos, accessPos, isBeeBed);
                    }
                }
            }
        }

        return bestTarget;
    }

    public static BlockPos findNearestBed(Level level, BlockPos origin, int horizontalRadius, int verticalRadius, boolean beeBedOnly) {
        if (level == null || origin == null) {
            return null;
        }

        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -horizontalRadius; dx <= horizontalRadius; dx++) {
            for (int dz = -horizontalRadius; dz <= horizontalRadius; dz++) {
                for (int dy = -verticalRadius; dy <= verticalRadius; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    boolean isBeeBed = isPolenBeeBedState(state);
                    if (beeBedOnly && !isBeeBed) {
                        continue;
                    }
                    if (!isUsableBedState(state)) {
                        continue;
                    }

                    BlockPos normalizedBedPos = normalizeBedPos(level, candidate);
                    if (normalizedBedPos == null) {
                        continue;
                    }

                    double score = normalizedBedPos.distSqr(origin);
                    if (isBeeBed) {
                        score -= POLEN_BEE_BED_PRIORITY_BONUS;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = normalizedBedPos.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    public static BlockPos findBestBedAccessPos(PolenEntity polen, BlockPos bedPos) {
        if (polen == null || bedPos == null || !isUsableBed(polen.level(), bedPos)) {
            return null;
        }

        Level level = polen.level();
        BlockPos normalizedBedPos = normalizeBedPos(level, bedPos);
        if (normalizedBedPos == null) {
            return null;
        }

        BlockState bedState = level.getBlockState(normalizedBedPos);
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        Direction bedFacing = null;
        if (bedState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            bedFacing = bedState.getValue(HorizontalDirectionalBlock.FACING);
        }

        Direction[] preferredDirections = bedFacing == null
                ? new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}
                : new Direction[] {
                bedFacing.getClockWise(),
                bedFacing.getCounterClockWise(),
                bedFacing.getOpposite(),
                bedFacing
        };

        for (Direction direction : preferredDirections) {
            BlockPos candidate = normalizedBedPos.relative(direction);
            double score = scoreBedAccessCandidate(polen, normalizedBedPos, candidate, -4.0D);
            if (score < bestScore) {
                bestScore = score;
                bestPos = candidate.immutable();
            }
        }

        for (int dx = -BED_ACCESS_RADIUS; dx <= BED_ACCESS_RADIUS; dx++) {
            for (int dz = -BED_ACCESS_RADIUS; dz <= BED_ACCESS_RADIUS; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos candidate = normalizedBedPos.offset(dx, dy, dz);
                    double score = scoreBedAccessCandidate(polen, normalizedBedPos, candidate, 0.0D);
                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = candidate.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    public static boolean isUsableBed(Level level, BlockPos pos) {
        return level != null && pos != null && isUsableBedState(level.getBlockState(pos));
    }

    public static BlockPos normalizeBedPos(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }

        BlockState state = level.getBlockState(pos);
        if (isPolenBeeBedState(state)) {
            return PolenBeeBedBlock.getHeadPos(state, pos).immutable();
        }
        return isUsableBedState(state) ? pos.immutable() : null;
    }

    public static BlockPos normalizeBedAnchorPos(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }

        BlockState state = level.getBlockState(pos);
        if (isPolenBeeBedState(state)) {
            return PolenBeeBedBlock.getFootPos(state, pos).immutable();
        }

        return normalizeBedPos(level, pos);
    }

    private static double scoreBedAccessCandidate(PolenEntity polen, BlockPos bedPos, BlockPos candidate, double bonus) {
        if (candidate == null
                || candidate.equals(bedPos)
                || !PolenSafetyEvaluator.isStandableSpot(polen, candidate)
                || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
            return Double.MAX_VALUE;
        }

        double score = candidate.distSqr(bedPos) * 1.8D + candidate.distSqr(polen.blockPosition()) * 0.20D + bonus;
        if (candidate.getY() == bedPos.getY()) {
            score -= 2.0D;
        }
        if (candidate.closerToCenterThan(polen.position(), 1.4D)) {
            score -= 3.0D;
        }
        return score;
    }

    private static boolean isUsableBedState(BlockState state) {
        return state != null && (state.is(BlockTags.BEDS) || isPolenBeeBedState(state));
    }

    private static boolean isPolenBeeBedState(BlockState state) {
        return state != null && state.is(ModBlocks.POLEN_BEE_BED.get());
    }
}
