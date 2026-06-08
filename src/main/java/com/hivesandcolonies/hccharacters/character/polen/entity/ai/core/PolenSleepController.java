package com.hivesandcolonies.hccharacters.character.polen.entity.ai.core;

import com.hivesandcolonies.hccharacters.bootstrap.registry.ModBlocks;
import com.hivesandcolonies.hccharacters.character.polen.block.PolenBeeBedBlock;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.PolenDangerMemoryTracker;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyEvaluator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class PolenSleepController {
    private static final long SLEEP_INTENT_LOCK_TICKS = 180L;
    private static final int NEARBY_BED_SCAN_RADIUS = 14;
    private static final int HOME_BED_SCAN_RADIUS = 14;
    private static final int BED_ACCESS_RADIUS = 3;
    private static final double START_SLEEP_DISTANCE = 5.0D;
    private static final double START_SLEEP_DISTANCE_SQR = START_SLEEP_DISTANCE * START_SLEEP_DISTANCE;
    private static final double MAX_FORCED_HOME_BED_DISTANCE_SQR = 128.0D * 128.0D;
    private static final double NEARBY_BED_PRIORITY_DISTANCE_SQR = 12.0D * 12.0D;
    private static final double POLEN_BEE_BED_PRIORITY_BONUS = 10000.0D;
    private static final double HOSTILE_THREAT_RANGE = 6.0D;
    private static final double UNTRUSTED_PLAYER_RANGE = 2.5D;

    private PolenSleepController() {
    }

    public static void tickServer(PolenEntity polen) {
        if (!(polen.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (polen.isSleeping()) {
            maintainOrWake(polen, serverLevel);
            return;
        }

        rememberNearbyBedAsRestAnchor(polen);

        if (!shouldSleepNow(polen)) {
            return;
        }

        BlockPos bedPos = findBestKnownBed(polen);
        if (bedPos == null) {
            return;
        }

        rememberBedAsRestAnchor(polen, bedPos);

        if (canStartSleepingAt(polen, bedPos)) {
            beginSleeping(polen, serverLevel, bedPos);
            return;
        }

        BlockPos accessPos = findBestBedAccessPos(polen, bedPos);
        setSleepIntentState(
                polen,
                serverLevel,
                bedPos,
                accessPos == null ? bedPos : accessPos,
                PolenSearchStatus.PATHING,
                "moving_to_bed"
        );
    }

    public static boolean shouldSleepNow(PolenEntity polen) {
        if (polen == null) {
            return false;
        }

        return (polen.level().isNight() || polen.level().isRaining())
                && !hasImmediateThreat(polen)
                && (polen.isSleeping() || polen.onGround())
                && !polen.isInWaterOrBubble();
    }

    public static boolean shouldPrioritizeBedReturn(PolenEntity polen) {
        return shouldReturnToSafeBedNow(polen) && findBestKnownBed(polen) != null;
    }

    public static boolean shouldReturnToSafeBedNow(PolenEntity polen) {
        if (polen == null) {
            return false;
        }
        return (polen.level().isNight() || polen.level().isRaining() || hasImmediateThreat(polen))
                && !polen.isSleeping()
                && polen.onGround()
                && !polen.isInWaterOrBubble();
    }

    public static boolean hasKnownBed(PolenEntity polen) {
        return findBestKnownBed(polen) != null;
    }

    public static BlockPos findBestKnownBed(PolenEntity polen) {
        if (polen == null) {
            return null;
        }

        BlockPos currentPos = polen.blockPosition();
        BlockPos preferredBeeBed = findNearestPolenBeeBed(polen, currentPos, HOME_BED_SCAN_RADIUS + NEARBY_BED_SCAN_RADIUS);
        if (preferredBeeBed != null) {
            return preferredBeeBed;
        }

        BlockPos nearbyBed = findNearestBed(polen, currentPos, NEARBY_BED_SCAN_RADIUS);
        BlockPos homeBed = findHomeBed(polen);

        if (homeBed != null && isUsableBed(polen.level(), homeBed)) {
            if (nearbyBed == null) {
                return homeBed;
            }

            double nearbyDistance = nearbyBed.distSqr(currentPos);
            double homeDistance = homeBed.distSqr(currentPos);
            if (nearbyDistance <= NEARBY_BED_PRIORITY_DISTANCE_SQR && nearbyDistance + 9.0D < homeDistance) {
                return nearbyBed;
            }

            if (homeDistance <= MAX_FORCED_HOME_BED_DISTANCE_SQR) {
                return homeBed;
            }
        }

        return nearbyBed;
    }

    public static BlockPos findHomeBed(PolenEntity polen) {
        if (polen == null) {
            return null;
        }

        BlockPos residenceUsePos = PolenHomeManager.getValidResidenceUsePos(polen);
        BlockPos best = findNearestBed(polen, residenceUsePos, HOME_BED_SCAN_RADIUS);
        if (best != null) {
            return best;
        }

        BlockPos residenceAnchorPos = polen.getAiState().getResidenceAnchorPos();
        best = findNearestBed(polen, residenceAnchorPos, HOME_BED_SCAN_RADIUS);
        if (best != null) {
            return best;
        }

        return findNearestBed(polen, polen.getAiState().getRestingPos(), HOME_BED_SCAN_RADIUS);
    }

    public static boolean tryBeginSleeping(PolenEntity polen, BlockPos bedPos, String note) {
        if (!(polen.level() instanceof ServerLevel serverLevel)
                || polen.isSleeping()
                || !shouldSleepNow(polen)
                || !canStartSleepingAt(polen, bedPos)) {
            return false;
        }

        beginSleeping(polen, serverLevel, bedPos, note == null || note.isBlank() ? "sleeping_in_bed" : note);
        return true;
    }

    private static void maintainOrWake(PolenEntity polen, ServerLevel serverLevel) {
        BlockPos sleepingPos = polen.getSleepingPos().orElse(null);
        if (!shouldSleepNow(polen) || !isUsableBed(polen.level(), sleepingPos)) {
            polen.stopSleeping();
            polen.getAiState().clearSearchState();
            return;
        }

        polen.stopQuietActivity();
        polen.getNavigation().stop();
        polen.setDeltaMovement(Vec3.ZERO);
        setSleepIntentState(
                polen,
                serverLevel,
                sleepingPos,
                sleepingPos,
                PolenSearchStatus.ARRIVED,
                "sleeping_in_bed"
        );
    }

    private static void beginSleeping(PolenEntity polen, ServerLevel serverLevel, BlockPos bedPos) {
        beginSleeping(polen, serverLevel, bedPos, "sleeping_in_bed");
    }

    private static void beginSleeping(PolenEntity polen, ServerLevel serverLevel, BlockPos bedPos, String note) {
        BlockPos normalizedBedPos = normalizeSleepPos(polen.level(), bedPos);
        if (normalizedBedPos == null) {
            return;
        }
        rememberBedAsRestAnchor(polen, normalizedBedPos);
        polen.stopQuietActivity();
        polen.getNavigation().stop();
        polen.setDeltaMovement(Vec3.ZERO);
        orientTowardBed(polen, normalizedBedPos);
        polen.startSleeping(normalizedBedPos);
        setSleepIntentState(polen, serverLevel, normalizedBedPos, normalizedBedPos, PolenSearchStatus.ARRIVED, note);
    }

    private static void setSleepIntentState(
            PolenEntity polen,
            ServerLevel serverLevel,
            BlockPos bedPos,
            BlockPos navigationTargetPos,
            PolenSearchStatus status,
            String note
    ) {
        long gameTime = serverLevel.getGameTime();
        polen.getAiState().getIntentState().set(
                PolenIntent.SEEK_REST,
                hasImmediateThreat(polen) ? "danger_returning_to_bed" : (polen.level().isRaining() ? "rain_returning_to_bed" : "night_returning_to_bed"),
                gameTime + SLEEP_INTENT_LOCK_TICKS
        );
        PolenTaskController.markActive(polen, PolenTaskType.SEEK_REST, note);
        polen.getAiState().setSearchState(
                PolenSearchType.REST,
                status == null ? PolenSearchStatus.PATHING : status,
                navigationTargetPos == null ? null : navigationTargetPos.immutable(),
                bedPos == null ? null : bedPos.immutable(),
                note
        );
    }

    public static boolean canStartSleepingAt(PolenEntity polen, BlockPos bedPos) {
        if (!isUsableBed(polen.level(), bedPos)) {
            return false;
        }

        BlockPos normalizedBedPos = normalizeSleepPos(polen.level(), bedPos);
        if (normalizedBedPos == null) {
            return false;
        }
        return normalizedBedPos.distSqr(polen.blockPosition()) <= START_SLEEP_DISTANCE_SQR
                || normalizedBedPos.closerToCenterThan(polen.position(), START_SLEEP_DISTANCE);
    }

    private static void rememberNearbyBedAsRestAnchor(PolenEntity polen) {
        if (polen.tickCount % 60 != 0) {
            return;
        }

        BlockPos rememberedHomeBed = findHomeBed(polen);
        BlockPos nearbyBed = rememberedHomeBed == null
                ? findNearestBed(polen, polen.blockPosition(), NEARBY_BED_SCAN_RADIUS)
                : rememberedHomeBed;
        BlockPos currentRest = polen.getAiState().getRestingPos();
        if (nearbyBed != null && (currentRest == null || currentRest.distSqr(nearbyBed) > 36.0D)) {
            rememberBedAsRestAnchor(polen, nearbyBed);
        }
    }

    private static void rememberBedAsRestAnchor(PolenEntity polen, BlockPos bedPos) {
        BlockPos accessPos = findBestBedAccessPos(polen, bedPos);
        BlockPos preferredRestPos = accessPos == null ? bedPos : accessPos;
        BlockPos normalizedRestingPos = PolenRoutinePlanner.normalizeRestingAnchor(polen, preferredRestPos);
        if (normalizedRestingPos != null) {
            polen.getAiState().setRestingPos(normalizedRestingPos.immutable());
        }
    }

    private static BlockPos findNearestPolenBeeBed(PolenEntity polen, BlockPos origin, int radius) {
        return findNearestBedMatching(polen, origin, radius, true);
    }

    private static BlockPos findNearestBed(PolenEntity polen, BlockPos origin, int radius) {
        return findNearestBedMatching(polen, origin, radius, false);
    }

    private static BlockPos findNearestBedMatching(PolenEntity polen, BlockPos origin, int radius, boolean beeBedOnly) {
        if (polen == null || origin == null) {
            return null;
        }

        Level level = polen.level();
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos candidate = origin.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(candidate);
                    boolean isBeeBed = isPolenBeeBedState(state);
                    if (beeBedOnly && !isBeeBed) {
                        continue;
                    }
                    if (!isUsableBedState(state)) {
                        continue;
                    }

                    BlockPos sleepPos = normalizeSleepPos(level, candidate);
                    if (sleepPos == null || findBestBedAccessPos(polen, sleepPos) == null) {
                        continue;
                    }

                    double score = sleepPos.distSqr(origin);
                    if (isBeeBed) {
                        score -= POLEN_BEE_BED_PRIORITY_BONUS;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = sleepPos.immutable();
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
        bedPos = normalizeSleepPos(level, bedPos);
        if (bedPos == null) {
            return null;
        }
        BlockState bedState = level.getBlockState(bedPos);
        BlockPos bestPos = null;
        double bestScore = Double.MAX_VALUE;

        Direction bedFacing = null;
        if (bedState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            bedFacing = bedState.getValue(HorizontalDirectionalBlock.FACING);
        }

        Direction[] preferredDirections = bedFacing == null
                ? new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}
                : new Direction[] {bedFacing.getClockWise(), bedFacing.getCounterClockWise(), bedFacing.getOpposite(), bedFacing};

        for (Direction direction : preferredDirections) {
            BlockPos candidate = bedPos.relative(direction);
            double score = scoreBedAccessCandidate(polen, bedPos, candidate, -4.0D);
            if (score < bestScore) {
                bestScore = score;
                bestPos = candidate.immutable();
            }
        }

        for (int dx = -BED_ACCESS_RADIUS; dx <= BED_ACCESS_RADIUS; dx++) {
            for (int dz = -BED_ACCESS_RADIUS; dz <= BED_ACCESS_RADIUS; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos candidate = bedPos.offset(dx, dy, dz);
                    double score = scoreBedAccessCandidate(polen, bedPos, candidate, 0.0D);
                    if (score < bestScore) {
                        bestScore = score;
                        bestPos = candidate.immutable();
                    }
                }
            }
        }

        return bestPos;
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

    public static boolean isUsableBed(Level level, BlockPos pos) {
        return level != null && pos != null && isUsableBedState(level.getBlockState(pos));
    }

    private static boolean isUsableBedState(BlockState state) {
        return state != null && (state.is(BlockTags.BEDS) || isPolenBeeBedState(state));
    }

    private static boolean isPolenBeeBedState(BlockState state) {
        return state != null && state.is(ModBlocks.POLEN_BEE_BED.get());
    }

    private static BlockPos normalizeSleepPos(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }

        BlockState state = level.getBlockState(pos);
        if (isPolenBeeBedState(state)) {
            return PolenBeeBedBlock.getHeadPos(state, pos).immutable();
        }
        return isUsableBedState(state) ? pos.immutable() : null;
    }

    private static void orientTowardBed(PolenEntity polen, BlockPos bedPos) {
        BlockPos normalizedBedPos = normalizeSleepPos(polen.level(), bedPos);
        BlockState state = polen.level().getBlockState(normalizedBedPos == null ? bedPos : normalizedBedPos);
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            polen.setYRot(state.getValue(HorizontalDirectionalBlock.FACING).toYRot());
            polen.yBodyRot = polen.getYRot();
            polen.yHeadRot = polen.getYRot();
        }
    }

    public static boolean hasImmediateThreat(PolenEntity polen) {
        boolean hostileNearby = !polen.level().getEntitiesOfClass(
                Monster.class,
                polen.getBoundingBox().inflate(HOSTILE_THREAT_RANGE),
                monster -> monster.isAlive() && monster.hasLineOfSight(polen)
        ).isEmpty();
        if (hostileNearby) {
            return true;
        }

        Player player = polen.level().getNearestPlayer(polen, UNTRUSTED_PLAYER_RANGE);
        return player != null && player.isAlive() && !polen.isComfortableWith(player);
    }
}
