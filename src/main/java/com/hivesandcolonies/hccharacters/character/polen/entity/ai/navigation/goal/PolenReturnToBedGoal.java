package com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.goal;

import com.hivesandcolonies.hccharacters.character.polen.entity.PolenEntity;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.brain.task.PolenTaskType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.core.PolenSleepController;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.PolenMovementHelper;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.safety.PolenSafetyNavigator;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchStatus;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.PolenSearchType;
import com.hivesandcolonies.hccharacters.character.polen.entity.ai.navigation.search.shelter.PolenShelterContextResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class PolenReturnToBedGoal extends Goal {
    private static final double MOVE_SPEED = 1.0D;
    private static final double BED_REACHED_DISTANCE_SQR = 5.0D * 5.0D;
    private static final double ACCESS_REACHED_DISTANCE_SQR = 2.0D * 2.0D;
    private static final double THREAT_COMMIT_DISTANCE_SQR = 6.0D * 6.0D;
    private static final int REPATH_COOLDOWN_TICKS = 12;
    private static final int STUCK_TICKS_BEFORE_REPATH = 24;
    private static final int STUCK_TICKS_BEFORE_BLINK = 55;
    private static final int BLINK_COOLDOWN_TICKS = 45;
    private static final int MAX_FAILED_REPATHS = 8;

    private final PolenEntity polen;

    private BlockPos bedPos;
    private BlockPos accessPos;
    private BlockPos navigationAnchorPos;
    private int repathCooldownTicks;
    private int blinkCooldownTicks;
    private int stuckTicks;
    private int failedRepathAttempts;
    private double lastDistanceSqr;
    private boolean reachedBed;

    public PolenReturnToBedGoal(PolenEntity polen) {
        this.polen = polen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!shouldReturnToBedNow()) {
            return false;
        }

        this.bedPos = PolenSleepController.findBestKnownBed(this.polen);
        this.accessPos = PolenMovementHelper.resolveReachableTarget(
                this.polen,
                PolenSleepController.findBestBedAccessPos(this.polen, this.bedPos),
                false
        );
        if (this.bedPos == null || this.accessPos == null) {
            return false;
        }

        if (shouldYieldToImmediateThreat()) {
            return false;
        }

        if (PolenSleepController.tryBeginSleeping(this.polen, this.bedPos, "sleeping_after_bed_return")) {
            return false;
        }

        resetPathState();
        this.navigationAnchorPos = null;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.polen.isSleeping()
                && this.bedPos != null
                && this.accessPos != null
                && this.failedRepathAttempts <= MAX_FAILED_REPATHS
                && shouldReturnToBedNow()
                && !shouldYieldToImmediateThreat()
                && PolenSleepController.isUsableBed(this.polen.level(), this.bedPos);
    }

    @Override
    public void start() {
        this.polen.stopQuietActivity();
        PolenTaskController.markActive(this.polen, PolenTaskType.SEEK_REST, "returning_to_bed");
        this.polen.getAiState().setSearchState(
                PolenSearchType.REST,
                PolenSearchStatus.PATHING,
                this.accessPos,
                this.bedPos,
                PolenSleepController.hasImmediateThreat(this.polen) ? "danger_pathing_to_bed" : (this.polen.level().isRaining() ? "rain_pathing_to_bed" : "night_pathing_to_bed")
        );
        this.lastDistanceSqr = distanceToAccessSqr();
        moveToAccessPos();
    }

    @Override
    public void tick() {
        if (this.bedPos == null || this.accessPos == null) {
            return;
        }

        if (shouldYieldToImmediateThreat()) {
            this.failedRepathAttempts = MAX_FAILED_REPATHS + 1;
            this.polen.getNavigation().stop();
            this.polen.getAiState().setSearchState(
                    PolenSearchType.REST,
                    PolenSearchStatus.FAILED,
                    this.accessPos,
                    this.bedPos,
                    "yielding_to_immediate_threat"
            );
            return;
        }

        Vec3 bedCenter = Vec3.atCenterOf(this.bedPos);
        PolenMovementHelper.steerLookTowardMovement(this.polen, this.accessPos, this.navigationAnchorPos, false);
        PolenShelterContextResolver.tryOpenNearbyDoor(this.polen.level(), this.polen.blockPosition(), 2);

        if (PolenSleepController.tryBeginSleeping(this.polen, this.bedPos, "sleeping_after_bed_return")) {
            this.reachedBed = true;
            return;
        }

        if (this.repathCooldownTicks > 0) {
            this.repathCooldownTicks--;
        }
        if (this.blinkCooldownTicks > 0) {
            this.blinkCooldownTicks--;
        }

        updateStuckCounter(distanceToAccessSqr());

        if (this.polen.distanceToSqr(bedCenter) <= BED_REACHED_DISTANCE_SQR
                || this.polen.distanceToSqr(Vec3.atCenterOf(this.accessPos)) <= ACCESS_REACHED_DISTANCE_SQR) {
            if (!PolenSleepController.shouldSleepNow(this.polen) && this.polen.hasPendingReturnHomeRequest()) {
                this.reachedBed = true;
                this.polen.clearReturnHomeRequest();
                this.polen.getNavigation().stop();
                this.polen.getAiState().setSearchState(
                        PolenSearchType.REST,
                        PolenSearchStatus.ARRIVED,
                        this.accessPos,
                        this.bedPos,
                        "requested_home_reached"
                );
                return;
            }
            this.accessPos = PolenSleepController.findBestBedAccessPos(this.polen, this.bedPos);
            PolenSleepController.tryBeginSleeping(this.polen, this.bedPos, "sleeping_after_bed_return");
            return;
        }

        if (this.repathCooldownTicks == 0
                && (this.polen.getNavigation().isDone() || this.stuckTicks >= STUCK_TICKS_BEFORE_REPATH)) {
            this.accessPos = PolenMovementHelper.resolveReachableTarget(
                    this.polen,
                    PolenSleepController.findBestBedAccessPos(this.polen, this.bedPos),
                    false
            );
            this.navigationAnchorPos = null;
            if (this.accessPos == null) {
                this.failedRepathAttempts = MAX_FAILED_REPATHS + 1;
                return;
            }

            boolean moved = moveToAccessPos();
            if (!moved) {
                this.failedRepathAttempts++;
            } else {
                this.failedRepathAttempts = 0;
            }
            this.repathCooldownTicks = REPATH_COOLDOWN_TICKS;
        }

        if (this.blinkCooldownTicks == 0 && this.stuckTicks >= STUCK_TICKS_BEFORE_BLINK) {
            if (PolenSafetyNavigator.tryBlinkTowardStandableSpot(this.polen, this.accessPos, 8)) {
                this.polen.getAiState().setSearchState(
                        PolenSearchType.REST,
                        PolenSearchStatus.BLINKING,
                        this.accessPos,
                        this.bedPos,
                        "blink_toward_bed_access"
                );
                this.blinkCooldownTicks = BLINK_COOLDOWN_TICKS;
                this.stuckTicks = 0;
                this.lastDistanceSqr = distanceToAccessSqr();
                moveToAccessPos();
            }
        }
    }

    @Override
    public void stop() {
        if (this.polen.isSleeping() || this.reachedBed) {
            PolenTaskController.markCompleted(this.polen, PolenTaskType.SEEK_REST, "bed_reached");
        } else if (this.bedPos != null && PolenSleepController.shouldSleepNow(this.polen)) {
            PolenTaskController.markActive(this.polen, PolenTaskType.SEEK_REST, "bed_return_repath_pending");
        }

        this.bedPos = null;
        this.accessPos = null;
        this.navigationAnchorPos = null;
        this.polen.getNavigation().stop();
        resetPathState();
    }

    private boolean shouldReturnToBedNow() {
        return this.polen.hasPendingReturnHomeRequest() || PolenSleepController.shouldReturnToSafeBedNow(this.polen);
    }

    private boolean shouldYieldToImmediateThreat() {
        if (!PolenSleepController.hasImmediateThreat(this.polen)) {
            return false;
        }

        if (this.bedPos == null || this.accessPos == null) {
            return true;
        }

        return this.polen.distanceToSqr(Vec3.atCenterOf(this.bedPos)) > THREAT_COMMIT_DISTANCE_SQR
                && this.polen.distanceToSqr(Vec3.atCenterOf(this.accessPos)) > THREAT_COMMIT_DISTANCE_SQR;
    }

    private void resetPathState() {
        this.repathCooldownTicks = 0;
        this.blinkCooldownTicks = 0;
        this.stuckTicks = 0;
        this.failedRepathAttempts = 0;
        this.lastDistanceSqr = Double.MAX_VALUE;
        this.reachedBed = false;
    }

    private boolean moveToAccessPos() {
        if (this.accessPos == null) {
            return false;
        }

        this.navigationAnchorPos = PolenMovementHelper.startAnchoredMove(
                this.polen,
                this.accessPos,
                this.navigationAnchorPos,
                MOVE_SPEED,
                false
        );
        boolean moved = this.navigationAnchorPos != null;

        this.polen.getAiState().setSearchState(
                PolenSearchType.REST,
                moved ? PolenSearchStatus.PATHING : PolenSearchStatus.FAILED,
                moved && this.navigationAnchorPos != null ? this.navigationAnchorPos : this.accessPos,
                this.bedPos,
                moved ? "following_bed_path" : "bed_path_failed"
        );
        if (!moved) {
            this.navigationAnchorPos = null;
        }
        return moved;
    }

    private void updateStuckCounter(double distanceSqr) {
        if (distanceSqr < this.lastDistanceSqr - 0.04D) {
            this.stuckTicks = 0;
        } else {
            this.stuckTicks++;
        }
        this.lastDistanceSqr = distanceSqr;
    }

    private double distanceToAccessSqr() {
        return this.accessPos == null ? Double.MAX_VALUE : this.polen.distanceToSqr(Vec3.atCenterOf(this.accessPos));
    }
}
