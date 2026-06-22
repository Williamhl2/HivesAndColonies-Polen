package com.hivesandcolonies.hccharacters.character.polen.entity.ai.core;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.intent.PolenIntent;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.routine.PolenRoutinePlanner;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentResolver;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.environment.PolenEnvironmentSnapshot;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenBedLocator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenBedTarget;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeManager;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.world.home.PolenHomeSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class PolenSleepController {
    private static final long SLEEP_INTENT_LOCK_TICKS = 180L;
    private static final int NEARBY_BED_SCAN_RADIUS = 14;
    private static final int HOME_BED_SCAN_RADIUS = 14;
    private static final double START_SLEEP_DISTANCE = 5.0D;
    private static final double START_SLEEP_DISTANCE_SQR = START_SLEEP_DISTANCE * START_SLEEP_DISTANCE;
    private static final double MAX_FORCED_HOME_BED_DISTANCE_SQR = 128.0D * 128.0D;
    private static final double NEARBY_BED_PRIORITY_DISTANCE_SQR = 12.0D * 12.0D;
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
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(polen);

        return (environment.night() || environment.raining())
                && !environment.immediateThreat()
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
        PolenEnvironmentSnapshot environment = PolenEnvironmentResolver.inspect(polen);
        return (environment.night() || environment.raining() || environment.immediateThreat())
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
        PolenBedTarget preferredBeeBed = PolenBedLocator.findNearestBedTarget(
                polen,
                currentPos,
                HOME_BED_SCAN_RADIUS + NEARBY_BED_SCAN_RADIUS,
                3,
                true
        );
        if (preferredBeeBed != null) {
            return preferredBeeBed.bedPos();
        }

        PolenBedTarget nearbyBed = PolenBedLocator.findNearestBedTarget(polen, currentPos, NEARBY_BED_SCAN_RADIUS, 3, false);
        PolenBedTarget homeBed = PolenHomeManager.getHomeSnapshot(polen).homeBed();

        if (homeBed != null && homeBed.bedPos() != null && isUsableBed(polen.level(), homeBed.bedPos())) {
            if (nearbyBed == null || nearbyBed.bedPos() == null) {
                return homeBed.bedPos();
            }

            double nearbyDistance = nearbyBed.bedPos().distSqr(currentPos);
            double homeDistance = homeBed.bedPos().distSqr(currentPos);
            if (nearbyDistance <= NEARBY_BED_PRIORITY_DISTANCE_SQR && nearbyDistance + 9.0D < homeDistance) {
                return nearbyBed.bedPos();
            }

            if (homeDistance <= MAX_FORCED_HOME_BED_DISTANCE_SQR) {
                return homeBed.bedPos();
            }
        }

        return nearbyBed == null ? null : nearbyBed.bedPos();
    }

    public static BlockPos findHomeBed(PolenEntity polen) {
        PolenHomeSnapshot snapshot = PolenHomeManager.getHomeSnapshot(polen);
        return snapshot.homeBed() == null ? null : snapshot.homeBed().bedPos();
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
        BlockPos normalizedBedPos = PolenBedLocator.normalizeBedPos(polen.level(), bedPos);
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
                PolenEnvironmentResolver.inspect(polen).immediateThreat()
                        ? "danger_returning_to_bed"
                        : (polen.level().isRaining() ? "rain_returning_to_bed" : "night_returning_to_bed"),
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

        BlockPos normalizedBedPos = PolenBedLocator.normalizeBedPos(polen.level(), bedPos);
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
                ? bedPos(PolenBedLocator.findNearestBedTarget(polen, polen.blockPosition(), NEARBY_BED_SCAN_RADIUS, 3, false))
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

    public static BlockPos findBestBedAccessPos(PolenEntity polen, BlockPos bedPos) {
        return PolenBedLocator.findBestBedAccessPos(polen, bedPos);
    }

    public static boolean isUsableBed(Level level, BlockPos pos) {
        return PolenBedLocator.isUsableBed(level, pos);
    }

    private static void orientTowardBed(PolenEntity polen, BlockPos bedPos) {
        BlockPos normalizedBedPos = PolenBedLocator.normalizeBedPos(polen.level(), bedPos);
        var state = polen.level().getBlockState(normalizedBedPos == null ? bedPos : normalizedBedPos);
        if (state.hasProperty(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING)) {
            polen.setYRot(state.getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING).toYRot());
            polen.yBodyRot = polen.getYRot();
            polen.yHeadRot = polen.getYRot();
        }
    }

    private static BlockPos bedPos(PolenBedTarget target) {
        return target == null ? null : target.bedPos();
    }

    public static boolean hasImmediateThreat(PolenEntity polen) {
        return PolenEnvironmentResolver.inspect(polen).immediateThreat();
    }
}
