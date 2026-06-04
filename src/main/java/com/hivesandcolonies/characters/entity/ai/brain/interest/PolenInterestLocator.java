package com.hivesandcolonies.characters.entity.ai.brain.interest;

import com.hivesandcolonies.characters.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.characters.entity.PolenEntity;
import com.hivesandcolonies.characters.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.characters.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class PolenInterestLocator {
    private static final int DEFAULT_LOCAL_RADIUS = 8;
    private static final int DEFAULT_LOCAL_HEIGHT = 3;
    private static final int LIGHT_LOCAL_RADIUS = 12;
    private static final int LIGHT_LOCAL_HEIGHT = 4;
    private static final int[] OBSERVE_Y_OFFSETS = {0, 1, -1, 2, -2};
    private static final int GENERAL_OBSERVE_RADIUS = 1;
    private static final int LIGHT_OBSERVE_RADIUS = 2;
    private static final int INTERESTING_LIGHT_EMISSION = 10;

    private PolenInterestLocator() {
    }

    public static PolenInterestTarget findPreferredInterest(PolenEntity polen, boolean includeSource) {
        long gameTime = polen.level().getGameTime();
        if (shouldPreferLightInterest(polen)) {
            PolenInterestTarget lightTarget = findPreferredInterestOfType(polen, PolenInterestType.LIGHT);
            if (lightTarget != null) {
                return lightTarget;
            }
        }

        PolenInterestTarget remembered = findRememberedInterest(polen, includeSource, gameTime);
        if (remembered != null) {
            return remembered;
        }

        return findNearestLocalInterest(polen, DEFAULT_LOCAL_RADIUS, DEFAULT_LOCAL_HEIGHT, includeSource, gameTime);
    }

    public static PolenInterestTarget findPreferredInterestOfType(PolenEntity polen, PolenInterestType type) {
        long gameTime = polen.level().getGameTime();
        BlockPos rememberedPos = switch (type) {
            case FLOWER -> polen.getAiState().getFavoriteFlowerPos();
            case HIVE -> polen.getAiState().getFavoriteHivePos();
            case SOURCE -> polen.getAiState().getFavoriteSourcePos();
            case LIGHT -> null;
        };
        BlockPos observePos = resolveObservationSpot(polen, rememberedPos, type);
        if (observePos != null
                && isRememberedInterestStillValid(polen, rememberedPos)
                && !polen.getAiState().isInterestTargetOnCooldown(rememberedPos, gameTime)) {
            return new PolenInterestTarget(rememberedPos.immutable(), observePos, type);
        }

        int radius = type == PolenInterestType.LIGHT ? LIGHT_LOCAL_RADIUS : DEFAULT_LOCAL_RADIUS;
        int height = type == PolenInterestType.LIGHT ? LIGHT_LOCAL_HEIGHT : DEFAULT_LOCAL_HEIGHT;
        return findNearestLocalInterestOfType(polen, radius, height, type, gameTime);
    }

    public static PolenInterestTarget findRememberedInterest(PolenEntity polen, boolean includeSource, long gameTime) {
        PolenInterestTarget best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        best = pickCloserRememberedInterest(
                polen,
                best,
                bestDistanceSqr,
                polen.getAiState().getFavoriteFlowerPos(),
                PolenInterestType.FLOWER,
                gameTime
        );
        if (best != null) {
            bestDistanceSqr = best.observePos().distSqr(polen.blockPosition());
        }

        best = pickCloserRememberedInterest(
                polen,
                best,
                bestDistanceSqr,
                polen.getAiState().getFavoriteHivePos(),
                PolenInterestType.HIVE,
                gameTime
        );
        if (best != null) {
            bestDistanceSqr = best.observePos().distSqr(polen.blockPosition());
        }

        if (includeSource) {
            best = pickCloserRememberedInterest(
                    polen,
                    best,
                    bestDistanceSqr,
                    polen.getAiState().getFavoriteSourcePos(),
                    PolenInterestType.SOURCE,
                    gameTime
            );
        }

        return best;
    }

    public static PolenInterestTarget findNearestLocalInterest(
            PolenEntity polen,
            int radius,
            int height,
            boolean includeSource,
            long gameTime
    ) {
        BlockPos origin = polen.blockPosition();
        PolenInterestTarget best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -height, -radius),
                origin.offset(radius, height, radius)
        )) {
            PolenInterestType type = classify(polen, pos, includeSource);
            BlockPos observePos = resolveObservationSpot(polen, pos, type);
            if (type == null
                    || observePos == null
                    || polen.getAiState().isInterestTargetOnCooldown(pos, gameTime)) {
                continue;
            }

            double distanceSqr = observePos.distSqr(origin) + pos.distSqr(observePos) * 0.35D;
            if (type == PolenInterestType.LIGHT && shouldPreferLightInterest(polen)) {
                distanceSqr -= 3.0D;
            }

            if (distanceSqr < bestDistanceSqr) {
                bestDistanceSqr = distanceSqr;
                best = new PolenInterestTarget(pos.immutable(), observePos, type);
            }
        }

        return best;
    }

    public static PolenInterestTarget findNearestLocalInterestOfType(
            PolenEntity polen,
            int radius,
            int height,
            PolenInterestType type,
            long gameTime
    ) {
        BlockPos origin = polen.blockPosition();
        PolenInterestTarget best = null;
        double bestDistanceSqr = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -height, -radius),
                origin.offset(radius, height, radius)
        )) {
            PolenInterestType candidateType = classify(polen, pos, true);
            BlockPos observePos = resolveObservationSpot(polen, pos, candidateType);
            if (candidateType != type
                    || observePos == null
                    || polen.getAiState().isInterestTargetOnCooldown(pos, gameTime)) {
                continue;
            }

            double distanceSqr = observePos.distSqr(origin) + pos.distSqr(observePos) * 0.35D;
            if (type == PolenInterestType.LIGHT && shouldPreferLightInterest(polen)) {
                distanceSqr -= 3.0D;
            }

            if (distanceSqr < bestDistanceSqr) {
                bestDistanceSqr = distanceSqr;
                best = new PolenInterestTarget(pos.immutable(), observePos, candidateType);
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

    public static PolenInterestType classify(PolenEntity polen, BlockPos pos, boolean includeSource) {
        if (polen == null || pos == null) {
            return null;
        }

        BlockState state = polen.level().getBlockState(pos);
        if (state.is(BlockTags.FLOWERS)) {
            return PolenInterestType.FLOWER;
        }

        if (state.is(Blocks.BEE_NEST) || state.is(Blocks.BEEHIVE)) {
            return PolenInterestType.HIVE;
        }

        if (includeSource && isSourceLike(state)) {
            return PolenInterestType.SOURCE;
        }

        if (isInterestingLightSource(polen.level(), pos, state) && shouldNoticeLightInterest(polen)) {
            return PolenInterestType.LIGHT;
        }

        return null;
    }

    private static PolenInterestTarget pickCloserRememberedInterest(
            PolenEntity polen,
            PolenInterestTarget currentBest,
            double currentBestDistanceSqr,
            BlockPos candidate,
            PolenInterestType type,
            long gameTime
    ) {
        BlockPos observePos = resolveObservationSpot(polen, candidate, type);
        if (!isRememberedInterestStillValid(polen, candidate)
                || observePos == null
                || polen.getAiState().isInterestTargetOnCooldown(candidate, gameTime)) {
            return currentBest;
        }

        double distanceSqr = observePos.distSqr(polen.blockPosition());
        if (currentBest == null || distanceSqr < currentBestDistanceSqr) {
            return new PolenInterestTarget(candidate.immutable(), observePos, type);
        }

        return currentBest;
    }

    private static boolean isRememberedInterestStillValid(PolenEntity polen, BlockPos pos) {
        if (pos == null || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, pos)) {
            return false;
        }

        return !polen.level().getBlockState(pos).isAir() || pos.closerToCenterThan(polen.position(), 2.0D);
    }

    private static boolean shouldPreferLightInterest(PolenEntity polen) {
        return polen.level().isNight()
                || polen.level().isRaining()
                || polen.level().getMaxLocalRawBrightness(polen.blockPosition()) <= 7;
    }

    private static boolean shouldNoticeLightInterest(PolenEntity polen) {
        return shouldPreferLightInterest(polen)
                || polen.getMood() == com.hivesandcolonies.characters.entity.ai.brain.mood.PolenMood.CURIOUS
                || polen.getMood() == com.hivesandcolonies.characters.entity.ai.brain.mood.PolenMood.INSPIRED
                || polen.getMood() == com.hivesandcolonies.characters.entity.ai.brain.mood.PolenMood.JOYFUL
                || polen.getMood() == com.hivesandcolonies.characters.entity.ai.brain.mood.PolenMood.CONFIDENT;
    }

    private static boolean isInterestingLightSource(Level level, BlockPos pos, BlockState state) {
        return !state.isAir()
                && !state.is(ModBlocks.POLEN_LANTERN.get())
                && level.getFluidState(pos).isEmpty()
                && state.getLightEmission() >= INTERESTING_LIGHT_EMISSION;
    }

    private static BlockPos resolveObservationSpot(PolenEntity polen, BlockPos focusPos, PolenInterestType type) {
        if (polen == null || focusPos == null || type == null) {
            return null;
        }

        Level level = polen.level();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;
        int radius = type == PolenInterestType.LIGHT ? LIGHT_OBSERVE_RADIUS : GENERAL_OBSERVE_RADIUS;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy : OBSERVE_Y_OFFSETS) {
                    BlockPos candidate = focusPos.offset(dx, dy, dz);
                    if (!PolenSafetyEvaluator.isSafeStandingSpot(polen, candidate)
                            || PolenDangerMemoryTracker.isDangerousMemorySpot(polen, candidate)) {
                        continue;
                    }

                    double score = candidate.distSqr(polen.blockPosition()) + candidate.distSqr(focusPos) * 0.45D;
                    if (type == PolenInterestType.LIGHT) {
                        score -= level.getMaxLocalRawBrightness(candidate) * 1.1D;
                        if (level.isRaining() && PolenSafetyEvaluator.isRainShelteredStandingSpot(level, candidate)) {
                            score -= 6.0D;
                        }
                    }

                    if (candidate.equals(focusPos) && type != PolenInterestType.LIGHT) {
                        score -= 0.75D;
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
